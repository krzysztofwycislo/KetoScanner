package app.carbpeek.server.http

import app.carbpeek.server.common.Outcome
import app.carbpeek.server.common.ServerError
import app.carbpeek.server.product.LookedUpProduct
import app.carbpeek.server.product.ProductLookup
import app.carbpeek.shared.contract.ApiJson
import app.carbpeek.shared.contract.ApiPaths
import app.carbpeek.shared.contract.ProductResponse
import app.carbpeek.shared.contract.normalizeBarcode
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

private const val FRESH_CACHE = "public, max-age=3600, s-maxage=86400"
private const val STALE_CACHE = "public, max-age=300, s-maxage=300"
private const val NOT_FOUND_CACHE = "public, max-age=300, s-maxage=600"
private const val BARCODE = "barcode"

internal fun Route.productRoutes(lookup: ProductLookup) {
    rateLimit(PRODUCT_RATE_LIMIT) {
        get("${ApiPaths.PRODUCTS}/{$BARCODE}") {
            val barcode = call.parameters[BARCODE]?.let(::normalizeBarcode)
                ?: throw ServerError.InvalidRequest.toException()
            when (val outcome = lookup.find(barcode)) {
                is Outcome.Success -> call.respondProduct(outcome.value)
                is Outcome.Failure -> throw outcome.error.toException()
            }
        }
    }
}

private suspend fun ApplicationCall.respondProduct(found: LookedUpProduct) {
    val json = ApiJson.encodeToString(
        serializer = ProductResponse.serializer(),
        value = found.product.toResponse(),
    )
    val cacheControl = if (found.isStale) {
        STALE_CACHE
    } else {
        FRESH_CACHE
    }
    response.header(name = HttpHeaders.CacheControl, value = cacheControl)
    respondText(text = json, contentType = ContentType.Application.Json)
}

private fun ServerError.toException(): ErrorResponseException =
    when (this) {
        ServerError.NotFound -> {
            ErrorResponseException(
                error = this,
                message = "Product not found",
                cacheControl = NOT_FOUND_CACHE,
            )
        }

        ServerError.InvalidRequest -> {
            ErrorResponseException(error = this, message = "Invalid barcode")
        }

        is ServerError.Unavailable -> {
            ErrorResponseException(error = this, message = "Product lookup unavailable")
        }
    }
