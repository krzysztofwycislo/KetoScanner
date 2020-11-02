package pl.handsome.club.openfoodapi

import pl.handsome.club.domain.product.ProductSearchState
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.openfoodapi.api.OpenFoodFactsApiProvider

class OpenFoodFactsRepository : ProductRepository {

    private val openFoodFactsApi = OpenFoodFactsApiProvider.getApi


    override suspend fun searchProductByBarcode(barcode: String): ProductSearchState {
        return openFoodFactsApi.getProduct(barcode)
            .let(::parseGetProductResponse)
    }

}