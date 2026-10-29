package pl.handsome.club.feature.product.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import pl.handsome.club.core.common.AppDispatchers
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.common.outcomeOf
import pl.handsome.club.core.database.product.ProductEntity
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.openfoodapi.ProductNetworkSource
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

internal val PRODUCT_CACHE_TTL: Duration = 30.days
internal const val PRODUCT_CACHE_MAX_ENTRIES = 300

internal class ProductRepositoryImpl(
    private val persistence: ProductPersistence,
    private val network: ProductNetworkSource,
    private val clock: Clock,
    private val dispatchers: AppDispatchers,
    private val applicationScope: CoroutineScope,
) : ProductRepository {

    private val refreshes = MutableStateFlow<Map<String, Deferred<Outcome<Unit>>>>(emptyMap())

    override fun observeProduct(barcode: String): Flow<Product> =
        persistence.observe(barcode)
            .filterNotNull()
            .map { it.toDomain() }
            .distinctUntilChanged()
            .flowOn(dispatchers.io)

    override suspend fun refreshProduct(barcode: String): Outcome<Unit> {
        val created = applicationScope.async(dispatchers.io, start = CoroutineStart.LAZY) { refreshIfStale(barcode) }
        val running = refreshes.updateAndGet { if (barcode in it) it else it + (barcode to created) }.getValue(barcode)

        if (running === created) {
            created.invokeOnCompletion { refreshes.update { if (it[barcode] === created) it - barcode else it } }
            created.start()
        } else {
            created.cancel()
        }
        return running.await()
    }

    private suspend fun refreshIfStale(barcode: String): Outcome<Unit> {
        val stored = outcomeOf(mapError = { AppError.Storage }) { persistence.find(barcode) }
        if (stored is Outcome.Failure) return stored

        val row = (stored as Outcome.Success).value
        if (row != null && row.isFresh()) return Outcome.Success(Unit)

        return when (val fetched = network.getProduct(barcode)) {
            is Outcome.Success -> save(fetched.value)
            is Outcome.Failure -> fetched
        }
    }

    private fun ProductEntity.isFresh(): Boolean {
        val now = clock.now()
        return fetchedAt <= now && now - fetchedAt < PRODUCT_CACHE_TTL
    }

    private suspend fun save(product: Product): Outcome<Unit> =
        outcomeOf(mapError = { AppError.Storage }) {
            persistence.save(product.toEntity(fetchedAt = clock.now()))
            persistence.keepNewest(PRODUCT_CACHE_MAX_ENTRIES)
        }
}
