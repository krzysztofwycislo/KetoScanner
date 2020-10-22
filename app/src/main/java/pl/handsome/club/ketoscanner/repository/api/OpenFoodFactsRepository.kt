package pl.handsome.club.ketoscanner.repository.api

import pl.handsome.club.domain.data.Product
import pl.handsome.club.ketoscanner.repository.ProductsRepository
import pl.handsome.club.ketoscanner.repository.convertToProduct
import pl.handsome.club.openfoodapi.OpenFoodFactsApiProvider

class OpenFoodFactsRepository : ProductsRepository {

    private val openFoodFactsApi = OpenFoodFactsApiProvider.getApi


    override suspend fun searchProductByBarcode(barcode: String): Product {
        return openFoodFactsApi.getProduct(barcode)
            .let(::convertToProduct)
    }

}