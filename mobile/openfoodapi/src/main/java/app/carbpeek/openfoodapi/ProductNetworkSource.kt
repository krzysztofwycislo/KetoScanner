package app.carbpeek.openfoodapi

import app.carbpeek.core.common.Outcome
import app.carbpeek.feature.product.api.model.Product

interface ProductNetworkSource {
    suspend fun getProduct(barcode: String): Outcome<Product>
}
