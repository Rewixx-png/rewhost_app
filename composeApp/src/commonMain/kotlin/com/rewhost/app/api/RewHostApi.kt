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
import kotlinx.serialization.serializer

class RewHostApi(
    private val client: HttpClient,
    private val settings: AppSettings
) {
    private val baseUrl = "https://rewhost.rewixx.ru/api/v1"
    private val jsonParser = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    init {
        client.plugin(HttpSend).intercept { request ->
            settings.getToken()?.let { request.header("X-Web-Access-Token", it) }
            execute(request)
        }
    }

    private suspend inline fun <reified T> get(endpoint: String): T {
        val response = client.get("$baseUrl$endpoint").body<JsonObject>()
        val dataElement = response["data"]
        val dataToDecode = if (dataElement != null && dataElement !is kotlinx.serialization.json.JsonNull) dataElement else response
        // FIX: Явно указываем сериализатор для T
        return jsonParser.decodeFromJsonElement(serializer<T>(), dataToDecode)
    }

    // Auth & User
    suspend fun generateLoginToken(): GenerateTokenResponse = get("/auth/generate-token")
    suspend fun checkLoginToken(loginToken: String): CheckTokenResponse = get("/auth/check-token/$loginToken")
    suspend fun login(token: String) {
        client.get("$baseUrl/user/dashboard") { header("X-Web-Access-Token", token) }
        settings.setToken(token)
    }
    suspend fun logout() { settings.clearToken() }
    suspend fun getDashboard(): DashboardResponse = get("/user/dashboard")
    suspend fun getNotifications(): List<NotificationItem> = get("/user/notifications")
    suspend fun markNotificationsRead() { client.post("$baseUrl/user/notifications/mark-read") }
    suspend fun clearNotifications() { client.delete("$baseUrl/user/notifications/clear") }
    suspend fun claimDailyBonus() { client.post("$baseUrl/user/bonus/claim") }


    // Containers
    suspend fun listContainers(): List<Container> = get("/user/containers")
    suspend fun getContainerDetails(id: Long): Container = get("/user/container/$id")
    suspend fun purchaseTariff(serverId: String, tariffId: String, imageId: String) {
        client.post("$baseUrl/user/tariffs/purchase") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("server_id" to serverId, "tariff_id" to tariffId, "image_id" to imageId))
        }
    }
    suspend fun containerAction(id: Long, action: String) {
        client.post("$baseUrl/user/container/$id/action") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("action" to action))
        }
    }
    suspend fun deleteContainer(id: Long) { client.delete("$baseUrl/user/container/$id/delete") }
    suspend fun reinstallContainer(id: Long) { client.post("$baseUrl/user/container/$id/reinstall/v2") }
    suspend fun renameContainer(id: Long, newName: String) {
        client.post("$baseUrl/user/container/$id/rename") { contentType(ContentType.Application.Json); setBody(mapOf("new_name" to newName)) }
    }
    suspend fun getContainerLogs(id: Long, lines: Int = 100): String = get("/user/container/$id/logs?lines=$lines")

    // Modules (Missing methods added)
    suspend fun getContainerModules(id: Long): List<JsonObject> = get("/user/modules/container/$id/modules")
    suspend fun getSavedModules(): List<JsonObject> = get("/user/modules/saved")
    suspend fun sendSavedModules() { client.post("$baseUrl/user/modules/saved/send") }
    suspend fun generateKey() { client.post("$baseUrl/user/keys/generate") }
    suspend fun revokeKeys() { client.post("$baseUrl/user/keys/revoke") }
    suspend fun activatePromo(code: String) {
        client.post("$baseUrl/user/promos/activate") { contentType(ContentType.Application.Json); setBody(mapOf("code" to code)) }
    }

    // Tools (Missing method added)
    suspend fun downloadSoundCloud(url: String): JsonObject {
        return client.post("$baseUrl/tools/soundcloud/download") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("url" to url))
        }.body()
    }

    // Games (All methods)
    suspend fun spinRoulette(): GameResult = get("/user/games/roulette/spin")
    suspend fun getMinesStatus(): GameState = get("/user/games/mines/status")
    suspend fun startMines(bet: Map<String, Any>): GameState = client.post("$baseUrl/user/games/mines/start") { contentType(ContentType.Application.Json); setBody(bet) }.body()
    suspend fun clickMines(cell: Map<String, Any>): GameState = client.post("$baseUrl/user/games/mines/click") { contentType(ContentType.Application.Json); setBody(cell) }.body()
    suspend fun cashoutMines(): GameResult = client.post("$baseUrl/user/games/mines/cashout").body()

    suspend fun getTowersStatus(): GameState = get("/user/games/towers/status")
    suspend fun startTowers(bet: Map<String, Any>): GameState = client.post("$baseUrl/user/games/towers/start") { contentType(ContentType.Application.Json); setBody(bet) }.body()
    suspend fun stepTowers(choice: Map<String, Any>): GameState = client.post("$baseUrl/user/games/towers/step") { contentType(ContentType.Application.Json); setBody(choice) }.body()
    suspend fun cashoutTowers(): GameResult = client.post("$baseUrl/user/games/towers/cashout").body()
    suspend fun playPlinko(bet: Map<String, Any>): GameResult = client.post("$baseUrl/user/games/plinko/play") { contentType(ContentType.Application.Json); setBody(bet) }.body()

    suspend fun placeCrashBet(amount: Double) { client.post("$baseUrl/user/games/crash/bet") { contentType(ContentType.Application.Json); setBody(mapOf("amount" to amount)) } }

    // Blackjack & Durak (Added missing methods)
    suspend fun getBlackjackRooms(): List<JsonObject> = get("/games/blackjack/rooms")
    suspend fun createBlackjackRoom() { client.post("$baseUrl/games/blackjack/rooms/create") }

    suspend fun getDurakRooms(): List<JsonObject> = get("/games/durak/rooms")
    suspend fun createDurakRoom() { client.post("$baseUrl/games/durak/rooms/create") }
    suspend fun createDurakPve() { client.post("$baseUrl/games/durak/rooms/create_pve") }

    // Admin & Public
    suspend fun getAdminUsers(page: Int = 0): List<UserProfile> {
        val response = client.get("$baseUrl/admin/users") { parameter("page", page) }.body<JsonObject>()
        // FIX: Manual deserialization for admin users
        val dataElement = response["data"]
        val dataToDecode = if (dataElement != null && dataElement !is kotlinx.serialization.json.JsonNull) dataElement else response
        return jsonParser.decodeFromJsonElement(AdminUsersResponse.serializer(), dataToDecode).users
    }
    suspend fun getAdminServers(): List<JsonObject> = get("/admin/servers/")
    suspend fun getServerStatus(): ServerStatusList = get("/public/server_status")
    fun getAvatarUrl(userId: Long): String = "$baseUrl/public/user_photo/$userId"

    // Support & Finance
    suspend fun getSupportTickets(): List<SupportTicket> = get("/user/support/tickets")
    suspend fun createSupportTicket(data: Map<String, Any>) {
        client.post("$baseUrl/user/support/create-ticket") {
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }
    suspend fun getTicketMessages(id: Long): List<TicketMessage> = get("/user/support/ticket/$id/messages")
    suspend fun replyToTicket(id: Long, message: Map<String, Any>) {
        client.post("$baseUrl/user/support/ticket/$id/reply") {
            contentType(ContentType.Application.Json)
            setBody(message)
        }
    }
    suspend fun createDeposit(amount: Double, method: String) {
        client.post("$baseUrl/user/deposit/create") { contentType(ContentType.Application.Json); setBody(mapOf("amount" to amount, "method" to method)) }
    }
}