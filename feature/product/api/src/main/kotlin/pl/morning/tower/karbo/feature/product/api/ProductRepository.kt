package pl.morning.tower.karbo.feature.product.api

import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.product.api.model.Product


interface ProductRepository {

    fun observeProduct(barcode: String): Flow<Product>

    suspend fun refreshProduct(barcode: String): Outcome<Unit>

}
