package com.rewhost.app.api

import com.rewhost.app.data.model.*
import com.rewhost.app.utils.AppSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer

class RewHostApi(
    private val client: HttpClient,
    private val settings: AppSettings
) {
    private val baseUrl = "https://rewhost.rewixx.ru/api/v1"

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    init {
        client.plugin(HttpSend).intercept { request ->
            val token = settings.getToken()
            if (!token.isNullOrBlank()) {
                request.header("X-Web-Access-Token", token)
            }
            execute(request)
        }
    }

    private suspend inline fun <reified T> get(endpoint: String): T {
        return client.get("$baseUrl$endpoint").body<JsonObject>().let { json ->
            val dataElement = json["data"] ?: json
            jsonParser.decodeFromJsonElement(serializer<T>(), dataElement)
        }
    }

    // ==================== AUTH ====================
    suspend fun generateLoginToken(): GenerateTokenResponse = get("/auth/generate-token")
    
    suspend fun checkLoginToken(loginToken: String): CheckTokenResponse = 
        get("/auth/check-token/$loginToken")

    suspend fun login(token: String) {
        try {
            client.get("$baseUrl/user/dashboard") {
                header("X-Web-Access-Token", token)
            }.body<JsonObject>()
            settings.setToken(token)
        } catch (e: ClientRequestException) {
            throw Exception("Auth failed: ${e.response.status}")
        }
    }

    suspend fun logout() {
        settings.clearToken()
    }

    // ==================== USER ====================
    suspend fun getDashboard(): DashboardResponse = get("/user/dashboard")
    
    suspend fun saveTelemetry(data: Map<String, Any>) {
        client.post("$baseUrl/user/telemetry") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    
    suspend fun uploadAvatar(avatar: ByteArray) {
        // Требует multipart/form-data - реализация зависит от UI
    }
    
    suspend fun getNotifications(): List<NotificationItem> = get("/user/notifications")
    
    suspend fun getUnreadNotificationCount(): Int = get("/user/notifications/unread-count")
    
    suspend fun markNotificationsAsRead() {
        client.post("$baseUrl/user/notifications/mark-read")
    }
    
    suspend fun clearAllNotifications() {
        client.delete("$baseUrl/user/notifications/clear")
    }
    
    suspend fun toggleUserSetting(setting: Map<String, Any>) {
        client.post("$baseUrl/user/settings/toggle") {
            contentType(ContentType.Application.Json)
            setBody(setting)
        }
    }
    
    suspend fun claimDailyBonus(data: Map<String, Any>) {
        client.post("$baseUrl/user/bonus/claim") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }

    // ==================== CONTAINERS ====================
    suspend fun listContainers(): List<Container> = get("/user/containers")
    
    suspend fun purchaseTariff() {
        client.post("$baseUrl/user/tariffs/purchase")
    }
    
    suspend fun getContainerDetails(id: Long): Container = get("/user/container/$id")
    
    suspend fun getContainerStats(id: Long): ContainerStats = get("/user/container/$id/stats")

    suspend fun containerAction(id: Long, action: String) {
        client.post("$baseUrl/user/container/$id/action") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("action" to action))
        }
    }
    
    suspend fun getContainerLogs(id: Long, lines: Int = 100): String {
        return client.get("$baseUrl/user/container/$id/logs") {
            parameter("lines", lines)
        }.body()
    }

    suspend fun renameContainer(id: Long, newName: String) {
        client.post("$baseUrl/user/container/$id/rename") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("new_name" to newName))
        }
    }

    suspend fun deleteContainer(id: Long) {
        client.delete("$baseUrl/user/container/$id/delete")
    }

    suspend fun reinstallContainer(id: Long) {
        client.post("$baseUrl/user/container/$id/reinstall/v2") {
            contentType(ContentType.Application.Json)
            setBody(JsonObject(emptyMap()))
        }
    }
    
    suspend fun createBackup(id: Long) {
        client.post("$baseUrl/user/container/$id/backup/create")
    }
    
    suspend fun restoreBackup(id: Long) {
        client.post("$baseUrl/user/container/$id/backup/restore")
    }
    
    suspend fun getBackupStatus(id: Long): String {
        return client.get("$baseUrl/user/container/$id/backup/status").body()
    }

    // ==================== SUPPORT ====================
    suspend fun getSupportTickets(): List<SupportTicket> = get("/user/support/tickets")
    
    suspend fun getSupportTicket(id: Long): SupportTicket = get("/user/support/ticket/$id")
    
    suspend fun getTicketMessages(id: Long): List<TicketMessage> = get("/user/support/ticket/$id/messages")
    
    suspend fun markTicketRead(id: Long) {
        client.post("$baseUrl/user/support/ticket/$id/mark-read")
    }
    
    suspend fun createSupportTicket(data: Map<String, Any>) {
        client.post("$baseUrl/user/support/create-ticket") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    
    suspend fun replyToTicket(id: Long, message: Map<String, Any>) {
        client.post("$baseUrl/user/support/ticket/$id/reply") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }
    }
    
    suspend fun closeTicket(id: Long) {
        client.post("$baseUrl/user/support/ticket/$id/close")
    }
    
    suspend fun hideTicket(id: Long) {
        client.post("$baseUrl/user/support/ticket/$id/hide")
    }

    // ==================== GAMES ====================
    suspend fun spinRoulette(): GameResult = get("/user/games/roulette/spin")
    
    suspend fun spinRouletteMultiple(): GameResult = get("/user/games/roulette/spin-multiple")
    
    suspend fun getMinesStatus(): GameState = get("/user/games/mines/status")
    
    suspend fun startMines(bet: Map<String, Any>): GameState {
        return client.post("$baseUrl/user/games/mines/start") {
            contentType(ContentType.Application.Json)
            setBody(bet)
        }.body()
    }
    
    suspend fun clickMines(cell: Map<String, Any>): GameState {
        return client.post("$baseUrl/user/games/mines/click") {
            contentType(ContentType.Application.Json)
            setBody(cell)
        }.body()
    }
    
    suspend fun cashoutMines(): GameResult = get("/user/games/mines/cashout")
    
    suspend fun getTowersStatus(): GameState = get("/user/games/towers/status")
    
    suspend fun startTowers(bet: Map<String, Any>): GameState {
        return client.post("$baseUrl/user/games/towers/start") {
            contentType(ContentType.Application.Json)
            setBody(bet)
        }.body()
    }
    
    suspend fun stepTowers(choice: Map<String, Any>): GameState {
        return client.post("$baseUrl/user/games/towers/step") {
            contentType(ContentType.Application.Json)
            setBody(choice)
        }.body()
    }
    
    suspend fun cashoutTowers(): GameResult = get("/user/games/towers/cashout")
    
    suspend fun playPlinko(bet: Map<String, Any>): GameResult {
        return client.post("$baseUrl/user/games/plinko/play") {
            contentType(ContentType.Application.Json)
            setBody(bet)
        }.body()
    }
    
    suspend fun getGameHistory(): List<GameHistoryItem> = get("/user/games/history")

    // ==================== FINANCE ====================
    suspend fun getFinanceHistory(): List<Transaction> = get("/user/finance/history")

    suspend fun createDeposit(amount: Double, method: String, details: Map<String, String>) {
        client.post("$baseUrl/user/deposit/create") {
            contentType(ContentType.Application.Json)
            setBody(CreateDepositRequest(amount, method, details))
        }
    }

    // ==================== KEYS ====================
    suspend fun generateKey(): String {
        return client.post("$baseUrl/user/keys/generate").body()
    }
    
    suspend fun revokeKeys() {
        client.post("$baseUrl/user/keys/revoke")
    }

    // ==================== MODULES ====================
    suspend fun listContainerModules(containerId: Long): ModulesList = 
        get("/user/modules/container/$containerId/modules")
    
    suspend fun backupContainerModules(containerId: Long, modules: List<String>) {
        client.post("$baseUrl/user/modules/container/$containerId/modules/backup") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("modules" to modules))
        }
    }
    
    suspend fun listSavedModules(): ModulesList = get("/user/modules/saved")
    
    suspend fun sendSavedModules(moduleIds: List<Long>) {
        client.post("$baseUrl/user/modules/saved/send") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("module_ids" to moduleIds))
        }
    }
    
    suspend fun deleteSavedModule(moduleId: Long) {
        client.delete("$baseUrl/user/modules/saved/$moduleId")
    }

    // ==================== PROMOS ====================
    suspend fun activatePromo(code: String) {
        client.post("$baseUrl/user/promos/activate") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("code" to code))
        }
    }
    
    suspend fun createPromo(data: Map<String, Any>) {
        client.post("$baseUrl/user/promos/create") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }

    // ==================== PUBLIC ====================
    suspend fun getPublicLogsData(token: String): LogsData = get("/public/logs/data/$token")
    
    suspend fun logClientError(level: String, message: String, context: String = "Frontend") {
        client.post("$baseUrl/public/client-log") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("level" to level, "message" to message, "context" to context))
        }
    }
    
    suspend fun getAppVersion(): VersionInfo = get("/public/version")
    
    suspend fun getServerStatus(): ServerStatusList = get("/public/server_status")
    
    suspend fun getSystemHealth(): SystemHealth = get("/public/system_health")
    
    suspend fun verifyFreeRequest(token: String): VerifyFreeResponse {
        return client.post("$baseUrl/public/verify-free-request/$token").body()
    }

    // ==================== ADMIN ====================
    suspend fun adminVerifyAccess(): String {
        return client.get("$baseUrl/admin/verify-access").body()
    }
    
    suspend fun getAdminDashboardStats(): String {
        return client.get("$baseUrl/admin/dashboard/stats").body()
    }
    
    suspend fun getAdminUsers(page: Int = 0, search: String? = null): List<UserProfile> {
        val response = client.get("$baseUrl/admin/users") {
            parameter("page", page)
            search?.let { parameter("search", it) }
        }.body<JsonObject>()
        val data = response["data"]?.jsonObject ?: response
        return jsonParser.decodeFromJsonElement(AdminUsersResponse.serializer(), data).users
    }
    
    suspend fun getAdminList(): String {
        return client.get("$baseUrl/admin/admins").body()
    }
    
    suspend fun getAdminUserDetails(userId: Long): String {
        return client.get("$baseUrl/admin/user/$userId").body()
    }
    
    suspend fun toggleUserBlock(userId: Long) {
        client.post("$baseUrl/admin/user/$userId/toggle-block")
    }
    
    suspend fun setUserRole(userId: Long, role: Map<String, Any>) {
        client.post("$baseUrl/admin/user/$userId/set-role") {
            contentType(ContentType.Application.Json)
            setBody(role)
        }
    }
    
    suspend fun updateUserBalance(userId: Long, data: Map<String, Any>) {
        client.post("$baseUrl/admin/user/$userId/balance") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    
    suspend fun deleteUser(userId: Long) {
        client.delete("$baseUrl/admin/user/$userId/delete")
    }

    suspend fun getAdminContainers(page: Int = 0, sortBy: String = "time", search: String? = null): List<Container> {
        val response = client.get("$baseUrl/admin/containers") {
            parameter("page", page)
            parameter("sort_by", sortBy)
            search?.let { parameter("search", it) }
        }.body<JsonObject>()
        val data = response["data"]?.jsonObject ?: response
        return jsonParser.decodeFromJsonElement(AdminContainersResponse.serializer(), data).containers
    }
    
    suspend fun getAdminUserContainers(userId: Long): String {
        return client.get("$baseUrl/admin/user/$userId/containers").body()
    }
    
    suspend fun adminGiveContainer(userId: Long, data: Map<String, Any>) {
        client.post("$baseUrl/admin/user/$userId/give-container") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    
    suspend fun adminFreezeContainer(containerId: Long) {
        client.post("$baseUrl/admin/container/$containerId/freeze")
    }
    
    suspend fun adminUnfreezeContainer(containerId: Long) {
        client.post("$baseUrl/admin/container/$containerId/unfreeze")
    }
    
    suspend fun adminAddTimeToContainer(containerId: Long, data: Map<String, Any>) {
        client.post("$baseUrl/admin/container/$containerId/add-time") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    
    suspend fun adminMigrateContainer(containerId: Long, data: Map<String, Any>) {
        client.post("$baseUrl/admin/container/$containerId/migrate") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    
    suspend fun adminDeleteContainer(containerId: Long) {
        client.delete("$baseUrl/admin/container/$containerId")
    }
    
    // Admin Support
    suspend fun adminGetSupportTickets(): String {
        return client.get("$baseUrl/admin/support/tickets").body()
    }
    
    suspend fun adminGetTicketDetails(ticketId: Long): String {
        return client.get("$baseUrl/admin/support/ticket/$ticketId").body()
    }
    
    suspend fun adminGetTicketMessages(ticketId: Long): String {
        return client.get("$baseUrl/admin/support/ticket/$ticketId/messages").body()
    }
    
    suspend fun adminMarkTicketRead(ticketId: Long) {
        client.post("$baseUrl/admin/support/ticket/$ticketId/mark-read")
    }
    
    suspend fun adminTakeTicket(ticketId: Long) {
        client.post("$baseUrl/admin/support/ticket/$ticketId/take")
    }
    
    suspend fun adminReplyTicket(ticketId: Long, message: Map<String, Any>) {
        client.post("$baseUrl/admin/support/ticket/$ticketId/reply") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }
    }
    
    suspend fun adminCloseTicket(ticketId: Long) {
        client.post("$baseUrl/admin/support/ticket/$ticketId/close")
    }
    
    // Admin Logs
    suspend fun adminGetLogs(
        page: Int = 0,
        actorId: Long? = null,
        targetId: Long? = null,
        type: String? = null,
        mode: String = "all"
    ): String {
        return client.get("$baseUrl/admin/logs") {
            parameter("page", page)
            actorId?.let { parameter("actor_id", it) }
            targetId?.let { parameter("target_id", it) }
            type?.let { parameter("type", it) }
            parameter("mode", mode)
        }.body()
    }
    
    // Admin System
    suspend fun adminRestartBot() {
        client.post("$baseUrl/admin/system/restart")
    }
    
    suspend fun adminClearCache() {
        client.post("$baseUrl/admin/system/clear-cache")
    }
    
    suspend fun adminGetSystemStatus(): String {
        return client.get("$baseUrl/admin/system/status").body()
    }
    
    suspend fun adminToggleMaintenance() {
        client.post("$baseUrl/admin/system/maintenance")
    }
    
    suspend fun adminToggleRaid() {
        client.post("$baseUrl/admin/system/raid")
    }
    
    // Admin Marketing
    suspend fun adminCreatePromo(data: Map<String, Any>) {
        client.post("$baseUrl/admin/marketing/promos/create") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    
    suspend fun adminSendBroadcast(message: Map<String, Any>) {
        client.post("$baseUrl/admin/marketing/broadcast/send") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }
    }
    
    // Admin Servers
    suspend fun adminGetServers(): String {
        return client.get("$baseUrl/admin/servers/").body()
    }
    
    suspend fun adminAddServer(serverData: Map<String, Any>) {
        client.post("$baseUrl/admin/servers/add") {
            contentType(ContentType.Application.Json)
            setBody(serverData)
        }
    }
    
    suspend fun adminToggleServer(serverId: String) {
        client.post("$baseUrl/admin/servers/$serverId/toggle")
    }
    
    suspend fun adminDeleteServer(serverId: String) {
        client.delete("$baseUrl/admin/servers/$serverId")
    }
    
    suspend fun adminEditServer(serverId: String, data: Map<String, Any>) {
        client.put("$baseUrl/admin/servers/$serverId") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    
    // Admin Finance
    suspend fun adminGetDepositRequest(requestId: Long): String {
        return client.get("$baseUrl/admin/finance/request/$requestId").body()
    }
    
    suspend fun adminApproveDepositRequest(requestId: Long) {
        client.post("$baseUrl/admin/finance/request/$requestId/approve")
    }
    
    suspend fun adminDeclineDepositRequest(requestId: Long, reason: Map<String, Any>) {
        client.post("$baseUrl/admin/finance/request/$requestId/decline") {
            contentType(ContentType.Application.Json)
            setBody(reason)
        }
    }

    fun getAvatarUrl(userId: Long): String = "$baseUrl/public/user_photo/$userId"
}