package app.carbpeek.shared.contract

import kotlinx.serialization.Serializable

@Serializable
data class ErrorEnvelope(
    val error: ApiError,
)

@Serializable
data class ApiError(
    val code: ErrorCode = ErrorCode.UNKNOWN,
    val message: String,
    val requestId: String? = null,
)

@Serializable
enum class ErrorCode {
    NOT_FOUND,
    INVALID_REQUEST,
    PAYLOAD_TOO_LARGE,
    UPGRADE_REQUIRED,
    RATE_LIMITED,
    UNAVAILABLE,
    INTERNAL,
    UNKNOWN,
}
