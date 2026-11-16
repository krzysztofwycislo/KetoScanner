package app.carbpeek.server.http

import app.carbpeek.server.db.DatabaseHealthCheck
import app.carbpeek.shared.contract.ApiPaths
import app.carbpeek.shared.contract.HealthResponse
import app.carbpeek.shared.contract.HealthStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.healthRoutes(healthCheck: DatabaseHealthCheck) {
    get(ApiPaths.HEALTH) {
        if (healthCheck.isUp()) {
            call.respond(
                status = HttpStatusCode.OK,
                message = HealthResponse(status = HealthStatus.UP, database = HealthStatus.UP),
            )
        } else {
            call.respond(
                status = HttpStatusCode.ServiceUnavailable,
                message = HealthResponse(status = HealthStatus.DOWN, database = HealthStatus.DOWN),
            )
        }
    }
}
