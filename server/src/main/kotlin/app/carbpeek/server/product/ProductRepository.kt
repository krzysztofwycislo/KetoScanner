package app.carbpeek.server.product

import app.carbpeek.server.common.Outcome
import kotlin.time.Instant

internal interface ProductRepository {
    suspend fun find(barcode: String): Outcome<StoredLookup?>

    suspend fun saveFound(
        product: Product,
        fetchedAt: Instant,
    ): Outcome<Unit>

    suspend fun saveMissing(
        barcode: String,
        checkedAt: Instant,
    ): Outcome<Unit>
}

internal sealed interface StoredLookup {
    data class Found(
        val product: Product,
        val fetchedAt: Instant,
    ) : StoredLookup

    data class Missing(
        val checkedAt: Instant,
    ) : StoredLookup
}
