package pl.handsome.club.openfoodapi

import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.product.api.model.Product

interface ProductNetworkSource {
    suspend fun getProduct(barcode: String): Outcome<Product>
}
