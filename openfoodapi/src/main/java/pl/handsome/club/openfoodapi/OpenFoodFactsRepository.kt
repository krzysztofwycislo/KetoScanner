package pl.handsome.club.openfoodapi

import pl.handsome.club.feature.product.api.model.ProductSearchState
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.openfoodapi.api.OpenFoodFactsApi
import retrofit2.HttpException


class OpenFoodFactsRepository(
    private val openFoodFactsApi: OpenFoodFactsApi
) : ProductRepository {

    override suspend fun searchProductByBarcode(barcode: String): ProductSearchState {
        val response = openFoodFactsApi.getProduct(barcode)

        // v2 answers an unknown barcode with HTTP 404
        if (response.code() == 404) {
            return ProductSearchState.NotFound
        }

        val body = response.body()
        if (!response.isSuccessful || body == null) {
            throw HttpException(response)
        }

        return parseGetProductResponse(body)
    }

}
