package pl.handsome.club.openfoodapi

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.withContext
import pl.handsome.club.core.common.AppDispatchers
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.common.fold
import pl.handsome.club.core.common.outcomeOf
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.openfoodapi.api.OpenFoodFactsApi
import pl.handsome.club.openfoodapi.data.GetProductResponse


class OpenFoodFactsProductSource(
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val dispatchers: AppDispatchers,
) : ProductNetworkSource {

    override suspend fun getProduct(barcode: String): Outcome<Product> =
        withContext(dispatchers.io) { fetchProduct(barcode) }

    private suspend fun fetchProduct(barcode: String): Outcome<Product> =
        outcomeOf(mapError = Throwable::toAppError) { toProduct(openFoodFactsApi.getProduct(barcode)) }
            .fold(onSuccess = { it }, onFailure = { Outcome.Failure(it) })

    private suspend fun toProduct(response: HttpResponse): Outcome<Product> {
        // v2 answers an unknown barcode with HTTP 404
        if (response.status == HttpStatusCode.NotFound) {
            return Outcome.Failure(AppError.NotFound)
        }

        if (!response.status.isSuccess()) {
            return Outcome.Failure(AppError.Http(response.status.value))
        }

        val product = parseGetProductResponse(response.body<GetProductResponse>())
            ?: return Outcome.Failure(AppError.NotFound)
        return Outcome.Success(product)
    }

}
