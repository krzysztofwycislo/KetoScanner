package pl.handsome.club.openfoodapi

import kotlinx.coroutines.withContext
import pl.handsome.club.core.common.AppDispatchers
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.common.fold
import pl.handsome.club.core.common.onSuccess
import pl.handsome.club.core.common.outcomeOf
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.openfoodapi.api.OpenFoodFactsApi
import pl.handsome.club.openfoodapi.data.GetProductResponse
import retrofit2.Response


class OpenFoodFactsRepository internal constructor(
    private val openFoodFactsApi: OpenFoodFactsApi,
    private val dispatchers: AppDispatchers,
    private val cache: ProductMemoryCache,
) : ProductRepository {

    constructor(openFoodFactsApi: OpenFoodFactsApi, dispatchers: AppDispatchers) :
        this(openFoodFactsApi, dispatchers, ProductMemoryCache())

    override suspend fun getProduct(barcode: String): Outcome<Product> {
        cache.get(barcode)?.let { return Outcome.Success(it) }

        return withContext(dispatchers.io) { fetchProduct(barcode) }
            .onSuccess { cache.put(barcode, it) }
    }

    private suspend fun fetchProduct(barcode: String): Outcome<Product> =
        outcomeOf(mapError = Throwable::toAppError) { openFoodFactsApi.getProduct(barcode) }
            .fold(onSuccess = ::toProduct, onFailure = { Outcome.Failure(it) })

    private fun toProduct(response: Response<GetProductResponse>): Outcome<Product> {
        // v2 answers an unknown barcode with HTTP 404
        if (response.code() == 404) {
            return Outcome.Failure(AppError.NotFound)
        }

        val body = response.body()
        if (!response.isSuccessful || body == null) {
            return Outcome.Failure(AppError.Http(response.code()))
        }

        val product = parseGetProductResponse(body) ?: return Outcome.Failure(AppError.NotFound)
        return Outcome.Success(product)
    }

}
