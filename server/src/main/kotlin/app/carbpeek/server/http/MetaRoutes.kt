package app.carbpeek.server.http

import app.carbpeek.server.config.MetaConfig
import app.carbpeek.shared.contract.ApiPaths
import app.carbpeek.shared.contract.MetaResponse
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

internal fun Route.metaRoutes(meta: MetaConfig) {
    get(ApiPaths.META) {
        call.respond(
            MetaResponse(
                apiVersion = ApiPaths.API_VERSION,
                serverVersion = meta.serverVersion,
                minSupportedVersionCode = meta.minSupportedVersionCode,
            ),
        )
    }
}
