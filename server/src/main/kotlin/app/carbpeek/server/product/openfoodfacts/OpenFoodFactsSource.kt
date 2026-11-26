package app.carbpeek.server.product.openfoodfacts

import app.carbpeek.server.common.Outcome
import app.carbpeek.server.common.ServerError
import app.carbpeek.server.common.fold
import app.carbpeek.server.common.outcomeOf
import app.carbpeek.server.product.Product
import app.carbpeek.server.product.ProductSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.slf4j.LoggerFactory
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

private const val BASE_URL = "https://world.openfoodfacts.org/"
private const val PRODUCT_FIELDS =
    "product_name,brands,image_url,serving_quantity,nutriments,categories_tags," +
        "ingredients,ingredients_text,nutriscore_grade,nova_group,lang"
private const val PRODUCT_LANGUAGE = "pl"
private const val STATUS_NOT_FOUND = 0
private val RETRY_AFTER = 30.seconds

internal class OpenFoodFactsSource(
    private val client: HttpClient,
) : ProductSource {
    private val logger = LoggerFactory.getLogger(OpenFoodFactsSource::class.java)

    override suspend fun fetch(barcode: String): Outcome<Product> =
        try {
            outcomeOf(mapError = { e -> unavailable(barcode = barcode, reason = e::class.java.name) }) {
                toOutcome(barcode = barcode, response = requestProduct(barcode))
            }.fold(onSuccess = { it }, onFailure = { Outcome.Failure(it) })
        } catch (e: CancellationException) {
            currentCoroutineContext().ensureActive()
            Outcome.Failure(unavailable(barcode = barcode, reason = e::class.java.name))
        }

    private suspend fun requestProduct(barcode: String): HttpResponse =
        client.get(BASE_URL) {
            url {
                appendPathSegments("api", "v2", "product", barcode)
                parameters.append("fields", PRODUCT_FIELDS)
                parameters.append("lc", PRODUCT_LANGUAGE)
            }
        }

    private suspend fun toOutcome(
        barcode: String,
        response: HttpResponse,
    ): Outcome<Product> {
        val isSuccess = response.status.isSuccess()
        if (!isSuccess && response.status != HttpStatusCode.NotFound) {
            return Outcome.Failure(unavailable(barcode = barcode, reason = "HTTP ${response.status.value}"))
        }
        val body = response.body<GetProductResponse>()
        return when {
            body.product != null && isSuccess -> {
                Outcome.Success(body.product.toProduct(barcode))
            }

            body.product == null && body.status == STATUS_NOT_FOUND -> {
                Outcome.Failure(ServerError.NotFound)
            }

            else -> {
                Outcome.Failure(
                    unavailable(
                        barcode = barcode,
                        reason = "HTTP ${response.status.value} with status ${body.status} in the body",
                    ),
                )
            }
        }
    }

    private fun unavailable(
        barcode: String,
        reason: String,
    ): ServerError {
        logger.warn("Open Food Facts request for {} failed: {}", barcode, reason)
        return ServerError.Unavailable(retryAfter = RETRY_AFTER)
    }
}
