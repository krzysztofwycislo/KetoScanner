package app.carbpeek.shared.contract

import kotlinx.serialization.json.Json

val ApiJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    encodeDefaults = true
}
