package app.carbpeek.server.product

import app.carbpeek.server.common.Outcome

internal interface ProductSource {
    suspend fun fetch(barcode: String): Outcome<Product>
}
