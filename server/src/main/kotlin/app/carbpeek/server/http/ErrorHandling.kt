package app.carbpeek.server.http

import app.carbpeek.server.common.ServerError
import app.carbpeek.shared.contract.ApiError
import app.carbpeek.shared.contract.ApiJson
import app.carbpeek.shared.contract.ErrorCode
import app.carbpeek.shared.contract.ErrorEnvelope
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.DurationUnit

private const val NO_STORE = "no-store"

internal class ErrorResponseException(
    val error: ServerError,
    override val message: String,
    val cacheControl: String = NO_STORE,
) : Exception(message)

private data class ErrorReply(
    val status: HttpStatusCode,
    val code: ErrorCode,
    val message: String,
    val cacheControl: String = NO_STORE,
)

internal fun Application.installErrorHandling() {
    val logger = log
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondError(
                ErrorReply(
                    status = status,
                    code = ErrorCode.NOT_FOUND,
                    message = "Not found",
                ),
            )
        }
        status(
            HttpStatusCode.MethodNotAllowed,
            HttpStatusCode.NotAcceptable,
            HttpStatusCode.UnsupportedMediaType,
        ) { call, status ->
            call.respondError(
                ErrorReply(
                    status = status,
                    code = ErrorCode.INVALID_REQUEST,
                    message = "Invalid request",
                ),
            )
        }
        status(HttpStatusCode.TooManyRequests) { call, status ->
            call.respondError(
                ErrorReply(
                    status = status,
                    code = ErrorCode.RATE_LIMITED,
                    message = "Too many requests",
                ),
            )
        }
        exception<ErrorResponseException> { call, cause ->
            call.respondError(cause)
        }
        exception<BadRequestException> { call, _ ->
            call.respondError(
                ErrorReply(
                    status = HttpStatusCode.BadRequest,
                    code = ErrorCode.INVALID_REQUEST,
                    message = "Invalid request",
                ),
            )
        }
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respondError(
                ErrorReply(
                    status = HttpStatusCode.InternalServerError,
                    code = ErrorCode.INTERNAL,
                    message = "Internal error",
                ),
            )
        }
    }
}

internal fun ApplicationCall.setRetryAfter(wait: Duration) {
    val seconds = ceil(wait.toDouble(DurationUnit.SECONDS)).toLong().coerceAtLeast(1)
    response.header(name = HttpHeaders.RetryAfter, value = seconds)
}

private suspend fun ApplicationCall.respondError(cause: ErrorResponseException) {
    val error = cause.error
    if (error is ServerError.Unavailable) {
        setRetryAfter(error.retryAfter)
    }
    respondError(
        ErrorReply(
            status = when (error) {
                ServerError.NotFound -> HttpStatusCode.NotFound
                ServerError.InvalidRequest -> HttpStatusCode.BadRequest
                is ServerError.Unavailable -> HttpStatusCode.ServiceUnavailable
            },
            code = when (error) {
                ServerError.NotFound -> ErrorCode.NOT_FOUND
                ServerError.InvalidRequest -> ErrorCode.INVALID_REQUEST
                is ServerError.Unavailable -> ErrorCode.UNAVAILABLE
            },
            message = cause.message,
            cacheControl = cause.cacheControl,
        ),
    )
}

private suspend fun ApplicationCall.respondError(reply: ErrorReply) {
    val envelope = ErrorEnvelope(
        error = ApiError(
            code = reply.code,
            message = reply.message,
            requestId = callId,
        ),
    )
    response.header(name = HttpHeaders.CacheControl, value = reply.cacheControl)
    respondText(
        text = ApiJson.encodeToString(serializer = ErrorEnvelope.serializer(), value = envelope),
        contentType = ContentType.Application.Json,
        status = reply.status,
    )
}
