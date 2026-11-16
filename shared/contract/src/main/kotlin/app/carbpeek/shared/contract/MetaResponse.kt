package app.carbpeek.shared.contract

import kotlinx.serialization.Serializable

@Serializable
data class MetaResponse(
    val apiVersion: Int,
    val serverVersion: String,
    val minSupportedVersionCode: Int,
)
