package com.rewhost.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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
data class AdminUsersResponse(
    val users: List<UserProfile> = emptyList()
)

@Serializable
data class AdminContainersResponse(
    val containers: List<Container> = emptyList()
)

// --- НОВЫЕ МОДЕЛИ ДЛЯ ФИНАНСОВ ---

@Serializable
data class Transaction(
    val id: Long? = null,
    val amount: Double,
    val method: String, // sbp, card, crypto, stars
    val status: String, // approved, pending, declined
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("decline_reason") val declineReason: String? = null
)

@Serializable
data class CreateDepositRequest(
    val amount: Double,
    val method: String,
    val details: Map<String, String>
)