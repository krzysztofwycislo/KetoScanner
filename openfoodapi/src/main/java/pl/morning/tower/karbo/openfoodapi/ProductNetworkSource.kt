package pl.morning.tower.karbo.openfoodapi

import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.product.api.model.Product

interface ProductNetworkSource {
    suspend fun getProduct(barcode: String): Outcome<Product>
}
