package pl.morning.tower.karbo.openfoodapi

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.withContext
import pl.morning.tower.karbo.core.common.AppDispatchers
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.fold
import pl.morning.tower.karbo.core.common.outcomeOf
import pl.morning.tower.karbo.feature.product.api.model.Product
import pl.morning.tower.karbo.openfoodapi.api.OpenFoodFactsApi
import pl.morning.tower.karbo.openfoodapi.data.GetProductResponse


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
