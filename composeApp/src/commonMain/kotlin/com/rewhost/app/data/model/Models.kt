package com.rewhost.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ==================== AUTH ====================
@Serializable
data class GenerateTokenResponse(
    val status: String? = null,
    @SerialName("login_token") val loginToken: String? = null,
    @SerialName("bot_username") val botUsername: String? = null
)

@Serializable
data class CheckTokenResponse(
    val status: String? = null,
    @SerialName("api_key") val apiKey: String? = null
)

// ==================== USER ====================
@Serializable
data class DashboardResponse(
    val profile: UserProfile,
    val containers: List<Container> = emptyList()
)

@Serializable
data class UserProfile(
    @SerialName("user_id") val userId: Long,
    @SerialName("username") val username: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    val balance: Double = 0.0,
    @SerialName("effective_role") val effectiveRole: Int = 0,
    @SerialName("level_info") val levelInfo: LevelInfo? = null
)

@Serializable
data class LevelInfo(
    val level: Int = 1,
    val xp: Long = 0,
    @SerialName("next_level_xp") val nextLevelXp: Long = 100,
    @SerialName("progress_percent") val progressPercent: Int = 0
)

@Serializable
data class Container(
    val id: Long,
    @SerialName("container_name") val containerName: String? = "Unknown",
    val status: String? = "unknown",
    @SerialName("image_info") val imageInfo: ImageInfo? = null,
    @SerialName("server_info") val serverInfo: ServerInfo? = null,
    @SerialName("tariff_info") val tariffInfo: TariffInfo? = null,
    val stats: ContainerStats? = null
)

@Serializable
data class TariffInfo(
    val name: String? = null
)

@Serializable
data class ImageInfo(
    val name: String? = null
)

@Serializable
data class ServerInfo(
    val name: String? = null
)

@Serializable
data class ContainerStats(
    @SerialName("cpu_usage") val cpuUsage: JsonElement? = null,
    @SerialName("memory_usage") val memoryUsage: JsonElement? = null
)

@Serializable
data class NotificationItem(
    val id: Long,
    val title: String,
    val message: String,
    @SerialName("created_at") val createdAt: String? = null,
    val read: Boolean = false
)

// ==================== SUPPORT ====================
@Serializable
data class SupportTicket(
    val id: Long,
    val title: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("unread_count") val unreadCount: Int = 0
)

@Serializable
data class TicketMessage(
    val id: Long,
    @SerialName("user_id") val userId: Long,
    val message: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("is_admin") val isAdmin: Boolean = false
)

// ==================== GAMES ====================
@Serializable
data class GameResult(
    val status: String,
    val amount: Double? = null,
    val multiplier: Double? = null
)

@Serializable
data class GameState(
    val status: String,
    val board: List<List<Int>>? = null,
    @SerialName("current_bet") val currentBet: Double? = null,
    @SerialName("current_multiplier") val currentMultiplier: Double? = null
)

@Serializable
data class GameHistoryItem(
    val id: Long,
    val game: String,
    val amount: Double,
    val result: String,
    @SerialName("created_at") val createdAt: String? = null
)

// ==================== FINANCE ====================
@Serializable
data class Transaction(
    val id: Long? = null,
    val amount: Double,
    val method: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("decline_reason") val declineReason: String? = null
)

@Serializable
data class CreateDepositRequest(
    val amount: Double,
    val method: String,
    val details: Map<String, String>
)

// ==================== MODULES ====================
@Serializable
data class ModulesList(
    val modules: List<ModuleInfo> = emptyList()
)

@Serializable
data class ModuleInfo(
    val id: Long? = null,
    val name: String,
    val size: Long? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// ==================== PUBLIC ====================
@Serializable
data class LogsData(
    val status: String,
    val data: String,
    val meta: Map<String, String>? = null
)

@Serializable
data class VersionInfo(
    val version: String,
    @SerialName("bot_username") val botUsername: String? = null
)

@Serializable
data class ServerStatusList(
    val status: String,
    val data: List<ServerStatusItem> = emptyList(),
    val meta: ServerStatusMeta? = null
)

@Serializable
data class ServerStatusItem(
    val id: String,
    val name: String,
    val status: String,
    val cpu: String? = null,
    val ram: String? = null,
    val disk: String? = null,
    val uptime: String? = null,
    @SerialName("top_load") val topLoad: String? = null,
    val ping: String? = null,
    val net: Boolean = false
)

@Serializable
data class ServerStatusMeta(
    @SerialName("last_updated") val lastUpdated: Long = 0,
    @SerialName("update_interval") val updateInterval: Long = 0
)

@Serializable
data class SystemHealth(
    val status: String,
    val data: HealthData? = null
)

@Serializable
data class HealthData(
    val api: ServiceHealth? = null,
    val database: ServiceHealth? = null
)

@Serializable
data class ServiceHealth(
    val status: String,
    val latency: Long = 0
)

@Serializable
data class VerifyFreeResponse(
    val status: String,
    val message: String? = null,
    @SerialName("bot_username") val botUsername: String? = null
)

// ==================== ADMIN ====================
@Serializable
data class AdminUsersResponse(
    val users: List<UserProfile> = emptyList()
)

@Serializable
data class AdminContainersResponse(
    val containers: List<Container> = emptyList()
)