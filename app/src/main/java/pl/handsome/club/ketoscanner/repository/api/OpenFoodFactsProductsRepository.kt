package pl.handsome.club.ketoscanner.repository.api

import pl.handsome.club.domain.data.Product
import pl.handsome.club.ketoscanner.repository.ProductsRepository

class OpenFoodFactsProductsRepository : ProductsRepository {

    val openFoodFactsApi = ApiProvider.openFoodFactsApi


    override suspend fun searchProductByName(name: String): Product {
        TODO("Not yet implemented")
    }

    override suspend fun searchProductByBarcode(barcode: String): Product {
        TODO("Not yet implemented")
    }

}