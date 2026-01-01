package com.rewhost.app.api

import com.rewhost.app.data.model.AdminContainersResponse
import com.rewhost.app.data.model.AdminUsersResponse
import com.rewhost.app.data.model.Container
import com.rewhost.app.data.model.CreateDepositRequest
import com.rewhost.app.data.model.DashboardResponse
import com.rewhost.app.data.model.Transaction
import com.rewhost.app.utils.AppSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
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

    @Serializable
    private data class LoginResponse(val message: String? = null, val detail: String? = null)

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

    private suspend inline fun <reified T> get(endpoint: String): T {
        return client.get("$baseUrl$endpoint").body<JsonObject>().let { json ->
            val dataElement = json["data"] ?: json
            jsonParser.decodeFromJsonElement(serializer<T>(), dataElement)
        }
    }

    suspend fun getDashboard(): DashboardResponse = get("/user/dashboard")

    suspend fun getContainerDetails(id: Long): Container = get<Container>("/user/container/$id")

    suspend fun containerAction(id: Long, action: String) {
        client.post("$baseUrl/user/container/$id/action") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("action" to action))
        }
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

    // --- FINANCE ---

    suspend fun getFinanceHistory(): List<Transaction> {
        // API возвращает список транзакций внутри 'data'
        return get<List<Transaction>>("/user/finance/history")
    }

    suspend fun createDeposit(amount: Double, method: String, details: Map<String, String>) {
        client.post("$baseUrl/user/deposit/create") {
            contentType(ContentType.Application.Json)
            setBody(CreateDepositRequest(amount, method, details))
        }
    }

    // --- ADMIN ---

    suspend fun getAdminUsers(): List<com.rewhost.app.data.model.UserProfile> {
        val response = client.get("$baseUrl/admin/users").body<JsonObject>()
        val data = response["data"]?.jsonObject ?: response
        return jsonParser.decodeFromJsonElement(AdminUsersResponse.serializer(), data).users
    }

    suspend fun getAdminContainers(): List<Container> {
        val response = client.get("$baseUrl/admin/containers").body<JsonObject>()
        val data = response["data"]?.jsonObject ?: response
        return jsonParser.decodeFromJsonElement(AdminContainersResponse.serializer(), data).containers
    }

    fun getAvatarUrl(userId: Long): String = "$baseUrl/public/user_photo/$userId"
}