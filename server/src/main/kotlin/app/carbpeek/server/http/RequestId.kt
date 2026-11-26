package app.carbpeek.server.http

import app.carbpeek.shared.contract.ApiHeaders
import app.carbpeek.shared.contract.ApiPaths
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.calllogging.processingTimeMillis
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import java.util.UUID

internal fun Application.installRequestId() {
    install(CallId) {
        generate { UUID.randomUUID().toString() }
        replyToHeader(ApiHeaders.REQUEST_ID)
    }
    install(CallLogging) {
        callIdMdc("requestId")
        filter { call -> call.request.path() != ApiPaths.HEALTH }
        format { call ->
            val method = call.request.httpMethod.value
            val status = call.response.status()?.value
            "$method ${call.request.path()} $status ${call.processingTimeMillis()} ms"
        }
    }
}
