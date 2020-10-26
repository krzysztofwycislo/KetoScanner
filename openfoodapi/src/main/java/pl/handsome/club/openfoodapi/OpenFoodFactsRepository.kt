package pl.handsome.club.openfoodapi

import pl.handsome.club.domain.data.Product
import pl.handsome.club.domain.repository.ProductsRepository
import pl.handsome.club.openfoodapi.api.OpenFoodFactsApiProvider

class OpenFoodFactsRepository : ProductsRepository {

    private val openFoodFactsApi = OpenFoodFactsApiProvider.getApi


    override suspend fun searchProductByBarcode(barcode: String): Product {
        return openFoodFactsApi.getProduct(barcode)
            .let(::convertToProduct)
    }

}