package app.carbpeek.server.http

import app.carbpeek.server.config.HttpConfig
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.RateLimiter
import io.ktor.server.request.header
import kotlin.time.Duration.Companion.minutes

private const val CLOUDFLARE_CLIENT_IP = "CF-Connecting-IP"

internal val PRODUCT_RATE_LIMIT = RateLimitName("products")

internal fun Application.installRateLimiting(config: HttpConfig) {
    install(RateLimit) {
        register(PRODUCT_RATE_LIMIT) {
            rateLimiter(limit = config.productRequestsPerMinutePerIp, refillPeriod = 1.minutes)
            requestKey { call -> call.clientIp(config) }
            modifyResponse { call, state ->
                if (state is RateLimiter.State.Exhausted) {
                    call.setRetryAfter(state.toWait)
                }
            }
        }
    }
}

private fun ApplicationCall.clientIp(config: HttpConfig): String =
    request
        .header(CLOUDFLARE_CLIENT_IP)
        ?.takeIf { config.behindCloudflare && it.isNotBlank() }
        ?: request.origin.remoteAddress
