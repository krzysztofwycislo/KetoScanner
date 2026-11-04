package pl.handsome.club.feature.product.api

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.product.api.model.Product


interface ProductRepository {

    fun observeProduct(barcode: String): Flow<Product>

    suspend fun refreshProduct(barcode: String): Outcome<Unit>

}
