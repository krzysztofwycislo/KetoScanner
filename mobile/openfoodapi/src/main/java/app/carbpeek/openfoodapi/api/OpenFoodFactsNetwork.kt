package app.carbpeek.openfoodapi.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


internal const val OPEN_FOOD_FACTS_BASE_URL = "https://world.openfoodfacts.org/"

internal val openFoodFactsJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun createOpenFoodFactsHttpClient(
    engine: HttpClientEngine,
    appName: String,
    appVersion: String,
    contact: String,
    enableBodyLogging: Boolean,
    connectTimeout: Duration = 10.seconds,
    readTimeout: Duration = 20.seconds,
    requestTimeout: Duration = 25.seconds,
): HttpClient =
    HttpClient(engine) {
        install(HttpTimeout) {
            connectTimeoutMillis = connectTimeout.inWholeMilliseconds
            socketTimeoutMillis = readTimeout.inWholeMilliseconds
            requestTimeoutMillis = requestTimeout.inWholeMilliseconds
        }
        install(UserAgent) {
            agent = openFoodFactsUserAgent(appName, appVersion, contact)
        }
        install(ContentNegotiation) {
            json(openFoodFactsJson)
        }
        if (enableBodyLogging) {
            install(Logging) {
                logger = Logger.ANDROID
                level = LogLevel.BODY
            }
        }
    }

fun createOpenFoodFactsImageHttpClient(
    engine: HttpClientEngine,
    appName: String,
    appVersion: String,
    contact: String,
    connectTimeout: Duration = 10.seconds,
    readTimeout: Duration = 30.seconds,
): HttpClient =
    HttpClient(engine) {
        install(HttpTimeout) {
            connectTimeoutMillis = connectTimeout.inWholeMilliseconds
            socketTimeoutMillis = readTimeout.inWholeMilliseconds
            requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
        }
        install(UserAgent) {
            agent = openFoodFactsUserAgent(appName, appVersion, contact)
        }
    }

internal fun openFoodFactsUserAgent(appName: String, appVersion: String, contact: String): String =
    "$appName/$appVersion ($contact)"
