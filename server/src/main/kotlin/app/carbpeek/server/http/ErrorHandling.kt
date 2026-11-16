package app.carbpeek.server.http

import app.carbpeek.shared.contract.ApiError
import app.carbpeek.shared.contract.ErrorCode
import app.carbpeek.shared.contract.ErrorEnvelope
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.installErrorHandling() {
    val logger = log
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondError(
                status = status,
                code = ErrorCode.NOT_FOUND,
                message = "Not found",
            )
        }
        exception<BadRequestException> { call, _ ->
            call.respondError(
                status = HttpStatusCode.BadRequest,
                code = ErrorCode.INVALID_REQUEST,
                message = "Invalid request",
            )
        }
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respondError(
                status = HttpStatusCode.InternalServerError,
                code = ErrorCode.INTERNAL,
                message = "Internal error",
            )
        }
    }
}

private suspend fun ApplicationCall.respondError(
    status: HttpStatusCode,
    code: ErrorCode,
    message: String,
) {
    val envelope = ErrorEnvelope(
        error = ApiError(
            code = code,
            message = message,
            requestId = callId,
        ),
    )
    respond(status = status, message = envelope)
}
