package app.carbpeek.server.product

import app.carbpeek.server.common.Outcome
import app.carbpeek.server.common.ServerError
import app.carbpeek.server.common.fold
import app.carbpeek.server.common.map
import app.carbpeek.server.config.ProductConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

internal class ProductLookupImpl(
    private val repository: ProductRepository,
    private val source: ProductSource,
    private val budget: RequestBudget,
    private val stats: LookupStats,
    private val config: ProductConfig,
    private val clock: Clock,
    private val scope: CoroutineScope,
) : ProductLookup {
    private val reservedForNewProducts = config.offRequestsPerMinute / 2
    private val fetches = ConcurrentHashMap<String, Deferred<Outcome<Product>>>()

    override suspend fun find(barcode: String): Outcome<LookedUpProduct> =
        when (val stored = findStored(barcode)) {
            is Outcome.Failure -> stored
            is Outcome.Success -> answer(barcode = barcode, stored = stored.value)
        }

    private suspend fun answer(
        barcode: String,
        stored: StoredLookup?,
    ): Outcome<LookedUpProduct> {
        val fresh = stored.freshAnswer()
        return when {
            fresh != null -> {
                stats.record(
                    fresh.fold(
                        onSuccess = { LookupEvent.FRESH_HIT },
                        onFailure = { LookupEvent.KNOWN_MISSING },
                    ),
                )
                fresh.map { LookedUpProduct(product = it, isStale = false) }
            }

            stored is StoredLookup.Found -> {
                stats.record(LookupEvent.STALE_HIT)
                startFetch(barcode = barcode, reserved = reservedForNewProducts)
                Outcome.Success(LookedUpProduct(product = stored.product, isStale = true))
            }

            else -> {
                stats.record(LookupEvent.MISS)
                startFetch(barcode = barcode, reserved = 0)
                    .await()
                    .map { LookedUpProduct(product = it, isStale = false) }
            }
        }
    }

    private fun startFetch(
        barcode: String,
        reserved: Int,
    ): Deferred<Outcome<Product>> {
        val created = scope.async(start = CoroutineStart.LAZY) {
            fetchUnlessFresh(barcode = barcode, reserved = reserved)
        }
        val running = fetches.putIfAbsent(barcode, created)
        if (running != null) {
            created.cancel()
            return running
        }
        created.invokeOnCompletion { fetches.remove(barcode, created) }
        created.start()
        return created
    }

    private suspend fun fetchUnlessFresh(
        barcode: String,
        reserved: Int,
    ): Outcome<Product> =
        when (val stored = findStored(barcode)) {
            is Outcome.Failure -> stored
            is Outcome.Success -> stored.value.freshAnswer() ?: fetch(barcode = barcode, reserved = reserved)
        }

    private suspend fun fetch(
        barcode: String,
        reserved: Int,
    ): Outcome<Product> {
        val wait = budget.tryAcquire(reserved)
        if (wait != null) {
            stats.record(LookupEvent.OFF_LIMIT_REACHED)
            return Outcome.Failure(ServerError.Unavailable(retryAfter = wait))
        }
        stats.record(LookupEvent.OFF_CALL)
        val fetched = source.fetch(barcode)
        when (fetched) {
            is Outcome.Success -> {
                recordFailure(repository.saveFound(product = fetched.value, fetchedAt = clock.now()))
            }

            is Outcome.Failure -> {
                if (fetched.error == ServerError.NotFound) {
                    recordFailure(repository.saveMissing(barcode = barcode, checkedAt = clock.now()))
                } else {
                    stats.record(LookupEvent.OFF_FAILURE)
                }
            }
        }
        return fetched
    }

    private suspend fun findStored(barcode: String): Outcome<StoredLookup?> =
        repository.find(barcode).also { recordFailure(it) }

    private fun recordFailure(databaseOutcome: Outcome<*>) {
        if (databaseOutcome is Outcome.Failure) {
            stats.record(LookupEvent.DATABASE_FAILURE)
        }
    }

    private fun StoredLookup?.freshAnswer(): Outcome<Product>? =
        when {
            this is StoredLookup.Found && isFresh() -> Outcome.Success(product)
            this is StoredLookup.Missing && isFresh() -> Outcome.Failure(ServerError.NotFound)
            else -> null
        }

    private fun StoredLookup.Found.isFresh(): Boolean = isWithin(since = fetchedAt, limit = config.freshFor)

    private fun StoredLookup.Missing.isFresh(): Boolean = isWithin(since = checkedAt, limit = config.notFoundFreshFor)

    private fun isWithin(
        since: Instant,
        limit: Duration,
    ): Boolean {
        val now = clock.now()
        return since <= now && now - since < limit
    }
}
