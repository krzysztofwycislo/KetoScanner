package app.carbpeek.server.product

import app.carbpeek.server.common.Outcome

internal interface ProductLookup {
    suspend fun find(barcode: String): Outcome<LookedUpProduct>
}

internal data class LookedUpProduct(
    val product: Product,
    val isStale: Boolean,
)
