package pl.handsome.club.feature.product.api

import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.product.api.model.Product


interface ProductRepository {

    suspend fun getProduct(barcode: String): Outcome<Product>

}
