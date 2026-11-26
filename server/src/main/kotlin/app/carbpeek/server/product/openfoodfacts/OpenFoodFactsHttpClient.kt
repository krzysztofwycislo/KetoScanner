package app.carbpeek.server.product.openfoodfacts

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val CONTACT = "support@carbpeek.app"

private val openFoodFactsJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

internal fun createOpenFoodFactsHttpClient(
    engine: HttpClientEngine,
    serverVersion: String,
    connectTimeout: Duration = 3.seconds,
    requestTimeout: Duration = 6.seconds,
): HttpClient =
    HttpClient(engine) {
        followRedirects = false
        install(HttpTimeout) {
            connectTimeoutMillis = connectTimeout.inWholeMilliseconds
            requestTimeoutMillis = requestTimeout.inWholeMilliseconds
        }
        install(UserAgent) {
            agent = "CarbPeek-Server/$serverVersion ($CONTACT)"
        }
        install(ContentNegotiation) {
            json(openFoodFactsJson)
        }
    }
