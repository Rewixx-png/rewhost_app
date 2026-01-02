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

// ==================== USER & DASHBOARD ====================
@Serializable
data class DashboardResponse(
    val profile: UserProfile,
    val containers: List<Container> = emptyList(),
    val servers: Map<String, ServerConfig> = emptyMap(),
    val tariffs: Map<String, TariffConfig> = emptyMap(),
    val images: Map<String, ImageConfig> = emptyMap(),
    val settings: UserSettings? = null
)

@Serializable
data class ServerConfig(
    val name: String,
    val ip: String? = null,
    val active: Boolean = true
)

@Serializable
data class TariffConfig(
    val name: String,
    @SerialName("price_rub") val price: Double,
    @SerialName("ram_mb") val ram: Int,
    @SerialName("disk_gb") val disk: Int
)

@Serializable
data class ImageConfig(
    val name: String,
    @SerialName("image_name") val dockerImage: String
)

@Serializable
data class UserSettings(@SerialName("show_id") val showId: Boolean = true)

@Serializable
data class UserProfile(
    @SerialName("user_id") val userId: Long,
    @SerialName("username") val username: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    val balance: Double = 0.0,
    @SerialName("ref_balance") val refBalance: Double = 0.0,
    @SerialName("effective_role") val effectiveRole: Int = 0,
    @SerialName("level_info") val levelInfo: LevelInfo? = null,
    @SerialName("has_used_free_tariff") val hasUsedFree: Boolean = false
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
    @SerialName("server_id") val serverId: String,
    @SerialName("tariff_id") val tariffId: String,
    @SerialName("image_id") val imageId: String,
    val status: String? = "unknown",
    @SerialName("remaining_seconds") val remainingSeconds: Long = 0,
    @SerialName("is_frozen") val isFrozen: Boolean = false,
    @SerialName("login_url") val loginUrl: String? = null,
    @SerialName("image_info") val imageInfo: ImageInfo? = null,
    @SerialName("server_info") val serverInfo: ServerInfo? = null,
    @SerialName("tariff_info") val tariffInfo: TariffInfo? = null,
    val stats: ContainerStats? = null
)

@Serializable data class TariffInfo(val name: String? = null)
@Serializable data class ImageInfo(val name: String? = null)
@Serializable data class ServerInfo(val name: String? = null)

@Serializable
data class ContainerStats(
    @SerialName("cpu_usage") val cpuUsage: Double = 0.0,
    // Исправили имя поля на ramUsage, как в коде UI
    @SerialName("ram_usage") val ramUsage: String? = "N/A"
)

@Serializable
data class NotificationItem(
    val id: Long,
    val text: String,
    val link: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("is_read") val read: Boolean = false
)

// ==================== SUPPORT ====================
@Serializable
data class SupportTicket(
    val id: Long,
    // Используем subject (как в БД), но добавляем алиас title для совместимости
    val subject: String? = null,
    val status: String,
    @SerialName("creation_date") val createdAt: String? = null,
    @SerialName("user_has_unread") val hasUnread: Boolean = false
) {
    // Вспомогательное свойство для UI
    val title: String get() = subject ?: "No Subject"
    val unreadCount: Int get() = if (hasUnread) 1 else 0
}

@Serializable
data class TicketMessage(
    val id: Long,
    @SerialName("sender_id") val senderId: Long,
    @SerialName("message_text") val message: String,
    val timestamp: String? = null,
    @SerialName("is_admin_message") val isAdmin: Boolean = false
)

// ==================== GAMES ====================
@Serializable
data class GameResult(
    val status: String,
    val amount: Double? = null,
    @SerialName("total_win") val totalWin: Double? = null,
    @SerialName("new_balance") val newBalance: Double? = null
)

@Serializable
data class GameState(
    val status: String,
    val board: List<List<Int>>? = null,
    val revealed: List<Int>? = null,
    @SerialName("current_row") val currentRow: Int? = null,
    val history: List<Int>? = null,
    @SerialName("bet_amount") val bet: Double? = null,
    val multiplier: Double? = null,
    @SerialName("current_win") val currentWin: Double? = null
)

@Serializable
data class ServerStatusList(
    val status: String,
    val data: List<ServerStatusItem> = emptyList()
)

@Serializable
data class ServerStatusItem(
    val id: String, val name: String, val status: String,
    val cpu: String? = null, val ram: String? = null,
    val ping: JsonElement? = null, val net: Boolean = false
)

// ==================== ADMIN ====================
@Serializable
data class AdminUsersResponse(
    val users: List<UserProfile> = emptyList()
)