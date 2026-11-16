package app.carbpeek.shared.contract

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: HealthStatus,
    val database: HealthStatus,
)

@Serializable
enum class HealthStatus {
    UP,
    DOWN,
}
