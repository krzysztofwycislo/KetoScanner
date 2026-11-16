package app.carbpeek.feature.product.api

import app.carbpeek.core.common.Outcome
import app.carbpeek.core.model.Barcode
import app.carbpeek.feature.product.api.model.Product
import kotlinx.coroutines.flow.Flow


interface ProductRepository {

    fun observeProduct(barcode: String): Flow<Product>

    fun observeProducts(barcodes: Set<Barcode>): Flow<List<Product>>

    suspend fun refreshProduct(barcode: String): Outcome<Unit>

}
