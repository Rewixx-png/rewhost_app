package com.rewhost.app.api

import com.rewhost.app.data.model.*
import com.rewhost.app.utils.AppSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.*
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

    // --- AUTH ---
    suspend fun generateLoginToken(): GenerateTokenResponse = get("/auth/generate-token")
    suspend fun checkLoginToken(loginToken: String): CheckTokenResponse = get("/auth/check-token/$loginToken")
    suspend fun login(token: String) {
        client.get("$baseUrl/user/dashboard") { header("X-Web-Access-Token", token) }
        settings.setToken(token)
    }
    suspend fun logout() { settings.clearToken() }

    // --- USER ---
    suspend fun getDashboard(): DashboardResponse = get("/user/dashboard")
    suspend fun getNotifications(): List<NotificationItem> = get("/user/notifications")
    suspend fun markNotificationsRead() { client.post("$baseUrl/user/notifications/mark-read") }
    suspend fun clearNotifications() { client.delete("$baseUrl/user/notifications/clear") }
    suspend fun claimDailyBonus() { client.post("$baseUrl/user/bonus/claim") }

    // --- CONTAINERS ---
    suspend fun listContainers(): List<Container> = get("/user/containers")
    suspend fun purchaseTariff() { client.post("$baseUrl/user/tariffs/purchase") }
    suspend fun getContainerDetails(id: Long): Container = get("/user/container/$id")
    suspend fun containerAction(id: Long, action: String) {
        client.post("$baseUrl/user/container/$id/action") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("action" to action))
        }
    }
    suspend fun deleteContainer(id: Long) { client.delete("$baseUrl/user/container/$id/delete") }
    suspend fun reinstallContainer(id: Long) { client.post("$baseUrl/user/container/$id/reinstall/v2") }
    suspend fun renameContainer(id: Long, newName: String) {
        client.post("$baseUrl/user/container/$id/rename") {
            parameter("name", newName)
        }
    }
    suspend fun getContainerLogs(id: Long, lines: Int = 100): String = get("/user/container/$id/logs?lines=$lines")

    // --- MODULES ---
    suspend fun getContainerModules(id: Long): List<JsonObject> = get("/user/modules/container/$id/modules")
    suspend fun backupModules(id: Long) { client.post("$baseUrl/user/modules/container/$id/modules/backup") }
    suspend fun getSavedModules(): List<JsonObject> = get("/user/modules/saved")
    suspend fun sendSavedModules() { client.post("$baseUrl/user/modules/saved/send") }
    suspend fun deleteSavedModule(moduleId: Long) { client.delete("$baseUrl/user/modules/saved/$moduleId") }

    // --- KEYS ---
    suspend fun generateKey() { client.post("$baseUrl/user/keys/generate") }
    suspend fun revokeKeys() { client.post("$baseUrl/user/keys/revoke") }

    // --- PROMOS ---
    suspend fun activatePromo(code: String) {
        client.post("$baseUrl/user/promos/activate") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("code" to code))
        }
    }

    // --- SUPPORT ---
    suspend fun getSupportTickets(): List<SupportTicket> = get("/user/support/tickets")
    suspend fun getTicketMessages(id: Long): List<TicketMessage> = get("/user/support/ticket/$id/messages")
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

    // --- GAMES ---
    suspend fun spinRoulette(): GameResult = get("/user/games/roulette/spin")
    
    suspend fun getMinesStatus(): GameState = get("/user/games/mines/status")
    suspend fun startMines(bet: Map<String, Any>): GameState = 
        client.post("$baseUrl/user/games/mines/start") {
            contentType(ContentType.Application.Json)
            setBody(bet)
        }.body()
    suspend fun clickMines(cell: Map<String, Any>): GameState = 
        client.post("$baseUrl/user/games/mines/click") {
            contentType(ContentType.Application.Json)
            setBody(cell)
        }.body()
    suspend fun cashoutMines(): GameResult = get("/user/games/mines/cashout")

    suspend fun getTowersStatus(): GameState = get("/user/games/towers/status")
    suspend fun startTowers(bet: Map<String, Any>): GameState = 
        client.post("$baseUrl/user/games/towers/start") {
            contentType(ContentType.Application.Json)
            setBody(bet)
        }.body()
    suspend fun stepTowers(choice: Map<String, Any>): GameState = 
        client.post("$baseUrl/user/games/towers/step") {
            contentType(ContentType.Application.Json)
            setBody(choice)
        }.body()
    suspend fun cashoutTowers(): GameResult = get("/user/games/towers/cashout")

    suspend fun playPlinko(bet: Map<String, Any>): GameResult = 
        client.post("$baseUrl/user/games/plinko/play") {
            contentType(ContentType.Application.Json)
            setBody(bet)
        }.body()

    // Blackjack
    suspend fun getBlackjackRooms(): List<JsonObject> = get("/games/blackjack/rooms")
    suspend fun createBlackjackRoom() { client.post("$baseUrl/games/blackjack/rooms/create") }

    // Durak
    suspend fun getDurakRooms(): List<JsonObject> = get("/games/durak/rooms")
    suspend fun createDurakRoom() { client.post("$baseUrl/games/durak/rooms/create") }
    suspend fun createDurakPve() { client.post("$baseUrl/games/durak/rooms/create_pve") }

    // --- BILLING ---
    suspend fun createDeposit(amount: Double, method: String) {
        client.post("$baseUrl/user/deposit/create") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("amount" to amount, "method" to method))
        }
    }
    
    // --- ADMIN ---
    suspend fun getAdminUsers(page: Int = 0): List<UserProfile> {
        val response = client.get("$baseUrl/admin/users") { parameter("page", page) }.body<JsonObject>()
        val data = response["data"]?.jsonObject ?: response
        return jsonParser.decodeFromJsonElement(AdminUsersResponse.serializer(), data).users
    }
    suspend fun getAdminServers(): List<JsonObject> = get("/admin/servers/")
    suspend fun toggleServer(id: String) { client.post("$baseUrl/admin/servers/$id/toggle") }
    suspend fun addServer(data: Map<String, Any>) { 
        client.post("$baseUrl/admin/servers/add") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }

    suspend fun getServerStatus(): ServerStatusList = get("/public/server_status")
    fun getAvatarUrl(userId: Long): String = "$baseUrl/public/user_photo/$userId"
}
