package app.carbpeek.server.product

import app.carbpeek.server.common.Outcome
import app.carbpeek.server.common.ServerError
import app.carbpeek.server.config.ProductConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TestTimeSource

private const val BARCODE = "3017620422003"
private const val OFF_LIMIT = 4
private val now = Instant.parse("2026-11-24T12:00:00Z")

class ProductLookupImplTest {

    private val repository = FakeProductRepository()
    private val source = FakeProductSource()
    private val stats = LookupStats()
    private val timeSource = TestTimeSource()
    private val budget = RequestBudget(limit = OFF_LIMIT, timeSource = timeSource)

    private val oldProduct = minimalProduct(barcode = BARCODE, name = "old")
    private val newProduct = minimalProduct(barcode = BARCODE, name = "new")
    private val unavailable = ServerError.Unavailable(retryAfter = 30.seconds)

    @Test
    fun `when a fresh product is stored then it is answered without a source call`() =
        runTest {
            repository.store(product = oldProduct, fetchedAt = now - 7.days + 1.milliseconds)

            val answer = createLookup().find(BARCODE)

            assertEquals(expected = found(product = oldProduct, isStale = false), actual = answer)
            runCurrent()
            assertEquals(expected = 0, actual = source.calls)
            assertEvents(LookupEvent.FRESH_HIT to 1)
        }

    @Test
    fun `when a stale product is stored then several requests get it at once and one refresh stores the new one`() =
        runTest {
            repository.store(product = oldProduct, fetchedAt = now - 7.days)
            source.gate = CompletableDeferred()
            source.answer = Outcome.Success(newProduct)
            val lookup = createLookup()

            val answers = List(3) { async { lookup.find(BARCODE) } }.awaitAll()

            runCurrent()
            assertEquals(expected = List(3) { found(product = oldProduct, isStale = true) }, actual = answers)
            assertEquals(expected = 1, actual = source.calls)
            source.gate?.complete(Unit)
            runCurrent()
            assertEquals(
                expected = StoredLookup.Found(product = newProduct, fetchedAt = now),
                actual = repository.stored[BARCODE],
            )
            assertEquals(expected = found(product = newProduct, isStale = false), actual = lookup.find(BARCODE))
            assertEquals(expected = 1, actual = source.calls)
            assertEvents(
                LookupEvent.STALE_HIT to 3,
                LookupEvent.OFF_CALL to 1,
                LookupEvent.FRESH_HIT to 1,
            )
        }

    @Test
    fun `when only the reserved part of the budget is left then a stale product is answered and not refreshed`() =
        runTest {
            repository.store(product = oldProduct, fetchedAt = now - 8.days)
            repeat(OFF_LIMIT / 2) { budget.tryAcquire() }

            val answer = createLookup().find(BARCODE)

            runCurrent()
            assertEquals(expected = found(product = oldProduct, isStale = true), actual = answer)
            assertEquals(expected = 0, actual = source.calls)
            assertEvents(LookupEvent.STALE_HIT to 1, LookupEvent.OFF_LIMIT_REACHED to 1)
        }

    @Test
    fun `when refreshing fails then the stored product stays and the next request refreshes again`() =
        runTest {
            repository.store(product = oldProduct, fetchedAt = now - 8.days)
            source.answer = Outcome.Failure(unavailable)
            val lookup = createLookup()

            val first = lookup.find(BARCODE)
            runCurrent()
            val second = lookup.find(BARCODE)
            runCurrent()

            assertEquals(expected = found(product = oldProduct, isStale = true), actual = first)
            assertEquals(expected = found(product = oldProduct, isStale = true), actual = second)
            assertEquals(
                expected = StoredLookup.Found(product = oldProduct, fetchedAt = now - 8.days),
                actual = repository.stored[BARCODE],
            )
            assertEvents(LookupEvent.STALE_HIT to 2, LookupEvent.OFF_CALL to 2, LookupEvent.OFF_FAILURE to 2)
        }

    @Test
    fun `when a stored product is no longer known by the source then it is replaced by not found`() =
        runTest {
            repository.store(product = oldProduct, fetchedAt = now - 8.days)
            source.answer = Outcome.Failure(ServerError.NotFound)
            val lookup = createLookup()

            val first = lookup.find(BARCODE)
            runCurrent()
            val second = lookup.find(BARCODE)

            assertEquals(expected = found(product = oldProduct, isStale = true), actual = first)
            assertEquals(expected = Outcome.Failure(ServerError.NotFound), actual = second)
            assertEquals(expected = StoredLookup.Missing(checkedAt = now), actual = repository.stored[BARCODE])
            assertEvents(LookupEvent.STALE_HIT to 1, LookupEvent.OFF_CALL to 1, LookupEvent.KNOWN_MISSING to 1)
        }

    @Test
    fun `when the stored time lies in the future then the product is stale`() =
        runTest {
            repository.store(product = oldProduct, fetchedAt = now + 1.hours)
            source.answer = Outcome.Success(newProduct)

            val answer = createLookup().find(BARCODE)

            runCurrent()
            assertEquals(expected = found(product = oldProduct, isStale = true), actual = answer)
            assertEquals(
                expected = StoredLookup.Found(product = newProduct, fetchedAt = now),
                actual = repository.stored[BARCODE],
            )
        }

    @Test
    fun `when the product is known to be missing then it is not found without a source call`() =
        runTest {
            repository.stored[BARCODE] = StoredLookup.Missing(checkedAt = now - 1.days + 1.milliseconds)

            val answer = createLookup().find(BARCODE)

            assertEquals(expected = Outcome.Failure(ServerError.NotFound), actual = answer)
            assertEquals(expected = 0, actual = source.calls)
            assertEvents(LookupEvent.KNOWN_MISSING to 1)
        }

    @Test
    fun `when the stored not found has expired then the source is asked again`() =
        runTest {
            repository.stored[BARCODE] = StoredLookup.Missing(checkedAt = now - 1.days)
            source.answer = Outcome.Success(newProduct)

            val answer = createLookup().find(BARCODE)

            assertEquals(expected = found(product = newProduct, isStale = false), actual = answer)
            assertEvents(LookupEvent.MISS to 1, LookupEvent.OFF_CALL to 1)
        }

    @Test
    fun `when nothing is stored then the product is fetched once and stored for the next request`() =
        runTest {
            source.answer = Outcome.Success(newProduct)
            val lookup = createLookup()

            val first = lookup.find(BARCODE)
            val second = lookup.find(BARCODE)

            assertEquals(expected = found(product = newProduct, isStale = false), actual = first)
            assertEquals(expected = first, actual = second)
            assertEquals(expected = listOf(BARCODE), actual = source.barcodes)
            assertEvents(LookupEvent.MISS to 1, LookupEvent.OFF_CALL to 1, LookupEvent.FRESH_HIT to 1)
        }

    @Test
    fun `when nothing is stored and the source does not know the product then not found is stored`() =
        runTest {
            source.answer = Outcome.Failure(ServerError.NotFound)
            val lookup = createLookup()

            val first = lookup.find(BARCODE)
            val second = lookup.find(BARCODE)

            assertEquals(expected = Outcome.Failure(ServerError.NotFound), actual = first)
            assertEquals(expected = first, actual = second)
            assertEquals(expected = StoredLookup.Missing(checkedAt = now), actual = repository.stored[BARCODE])
            assertEquals(expected = 1, actual = source.calls)
            assertEvents(LookupEvent.MISS to 1, LookupEvent.OFF_CALL to 1, LookupEvent.KNOWN_MISSING to 1)
        }

    @Test
    fun `when nothing is stored and the source fails then the failure is answered and nothing is stored`() =
        runTest {
            source.answer = Outcome.Failure(unavailable)

            val answer = createLookup().find(BARCODE)

            assertEquals(expected = Outcome.Failure(unavailable), actual = answer)
            assertTrue(repository.stored.isEmpty())
            assertEvents(LookupEvent.MISS to 1, LookupEvent.OFF_CALL to 1, LookupEvent.OFF_FAILURE to 1)
        }

    @Test
    fun `when the budget is used up then an unstored product is unavailable until a request is given back`() =
        runTest {
            repeat(OFF_LIMIT) { budget.tryAcquire() }
            timeSource += 15.seconds

            val answer = createLookup().find(BARCODE)

            assertEquals(
                expected = Outcome.Failure(ServerError.Unavailable(retryAfter = 45.seconds)),
                actual = answer,
            )
            assertEquals(expected = 0, actual = source.calls)
            assertEvents(LookupEvent.MISS to 1, LookupEvent.OFF_LIMIT_REACHED to 1)
        }

    @Test
    fun `when only the reserved part of the budget is left then an unstored product is still fetched`() =
        runTest {
            repeat(OFF_LIMIT / 2) { budget.tryAcquire() }
            source.answer = Outcome.Success(newProduct)

            val answer = createLookup().find(BARCODE)

            assertEquals(expected = found(product = newProduct, isStale = false), actual = answer)
        }

    @Test
    fun `when several requests miss at the same time then they share one source call and one budget unit`() =
        runTest {
            repeat(OFF_LIMIT - 1) { budget.tryAcquire() }
            source.gate = CompletableDeferred()
            source.answer = Outcome.Success(newProduct)
            val lookup = createLookup()
            val calls = List(5) { async { lookup.find(BARCODE) } }
            runCurrent()

            source.gate?.complete(Unit)

            assertEquals(
                expected = List(5) { found(product = newProduct, isStale = false) },
                actual = calls.awaitAll(),
            )
            assertEquals(expected = 1, actual = source.calls)
            assertEvents(LookupEvent.MISS to 5, LookupEvent.OFF_CALL to 1)
        }

    @Test
    fun `when a request read the database before another one stored the product then it does not call the source`() =
        runTest {
            source.answer = Outcome.Success(newProduct)
            val lookup = createLookup()
            val readGate = CompletableDeferred<Unit>()
            repository.nextReadGate = readGate
            val slow = async { lookup.find(BARCODE) }
            runCurrent()
            lookup.find(BARCODE)

            readGate.complete(Unit)

            assertEquals(expected = found(product = newProduct, isStale = false), actual = slow.await())
            assertEquals(expected = 1, actual = source.calls)
        }

    @Test
    fun `when one of the waiting requests is cancelled then the others still get the product`() =
        runTest {
            source.gate = CompletableDeferred()
            source.answer = Outcome.Success(newProduct)
            val lookup = createLookup()
            val cancelled = async { lookup.find(BARCODE) }
            runCurrent()
            val waiting = async { lookup.find(BARCODE) }
            runCurrent()

            cancelled.cancelAndJoin()
            source.gate?.complete(Unit)

            assertEquals(expected = found(product = newProduct, isStale = false), actual = waiting.await())
            assertTrue(cancelled.isCancelled)
            assertEquals(expected = 1, actual = source.calls)
        }

    @Test
    fun `when the only waiting request is cancelled then the product is still stored`() =
        runTest {
            source.gate = CompletableDeferred()
            source.answer = Outcome.Success(newProduct)
            val lookup = createLookup()
            val cancelled = async { lookup.find(BARCODE) }
            runCurrent()

            cancelled.cancelAndJoin()
            source.gate?.complete(Unit)
            runCurrent()

            assertEquals(
                expected = StoredLookup.Found(product = newProduct, fetchedAt = now),
                actual = repository.stored[BARCODE],
            )
        }

    @Test
    fun `when different barcodes are missed at the same time then each one is fetched`() =
        runTest {
            source.answer = Outcome.Failure(ServerError.NotFound)
            val lookup = createLookup()

            listOf("00000001", "00000002").map { async { lookup.find(it) } }.awaitAll()

            assertEquals(expected = listOf("00000001", "00000002"), actual = source.barcodes)
        }

    @Test
    fun `when reading fails then the failure is answered and the source is not called`() =
        runTest {
            repository.readFailure = unavailable

            val answer = createLookup().find(BARCODE)

            runCurrent()
            assertEquals(expected = Outcome.Failure(unavailable), actual = answer)
            assertEquals(expected = 0, actual = source.calls)
            assertEvents(LookupEvent.DATABASE_FAILURE to 1)
        }

    @Test
    fun `when saving fails after a source call then the product is still answered`() =
        runTest {
            repository.saveFailure = unavailable
            source.answer = Outcome.Success(newProduct)

            val answer = createLookup().find(BARCODE)

            assertEquals(expected = found(product = newProduct, isStale = false), actual = answer)
            assertEvents(LookupEvent.MISS to 1, LookupEvent.OFF_CALL to 1, LookupEvent.DATABASE_FAILURE to 1)
        }

    @Test
    fun `when storing not found fails then not found is still answered`() =
        runTest {
            repository.saveFailure = unavailable
            source.answer = Outcome.Failure(ServerError.NotFound)

            val answer = createLookup().find(BARCODE)

            assertEquals(expected = Outcome.Failure(ServerError.NotFound), actual = answer)
            assertEvents(LookupEvent.MISS to 1, LookupEvent.OFF_CALL to 1, LookupEvent.DATABASE_FAILURE to 1)
        }

    private fun TestScope.createLookup(): ProductLookupImpl =
        ProductLookupImpl(
            repository = repository,
            source = source,
            budget = budget,
            stats = stats,
            config = ProductConfig(
                freshFor = 7.days,
                notFoundFreshFor = 1.days,
                offRequestsPerMinute = OFF_LIMIT,
            ),
            clock = FixedClock,
            scope = backgroundScope,
        )

    private fun found(
        product: Product,
        isStale: Boolean,
    ): Outcome<LookedUpProduct> = Outcome.Success(LookedUpProduct(product = product, isStale = isStale))

    private fun assertEvents(vararg expected: Pair<LookupEvent, Int>) {
        assertEquals(
            expected = expected.associate { (event, count) -> event to count.toLong() },
            actual = stats.takeCounts().filterValues { it > 0 },
        )
    }
}

private object FixedClock : Clock {
    override fun now(): Instant = app.carbpeek.server.product.now
}

private class FakeProductSource : ProductSource {
    val barcodes = mutableListOf<String>()
    val calls: Int get() = barcodes.size
    var gate: CompletableDeferred<Unit>? = null
    var answer: Outcome<Product>? = null

    override suspend fun fetch(barcode: String): Outcome<Product> {
        barcodes += barcode
        gate?.await()
        return checkNotNull(answer) { "The source was not expected to be called" }
    }
}

private class FakeProductRepository : ProductRepository {
    val stored = mutableMapOf<String, StoredLookup>()
    var readFailure: ServerError? = null
    var saveFailure: ServerError? = null
    var nextReadGate: CompletableDeferred<Unit>? = null

    fun store(
        product: Product,
        fetchedAt: Instant,
    ) {
        stored[product.barcode] = StoredLookup.Found(product = product, fetchedAt = fetchedAt)
    }

    override suspend fun find(barcode: String): Outcome<StoredLookup?> {
        val result = stored[barcode]
        val gate = nextReadGate
        nextReadGate = null
        gate?.await()
        return readFailure?.let { Outcome.Failure(it) } ?: Outcome.Success(result)
    }

    override suspend fun saveFound(
        product: Product,
        fetchedAt: Instant,
    ): Outcome<Unit> = save { store(product = product, fetchedAt = fetchedAt) }

    override suspend fun saveMissing(
        barcode: String,
        checkedAt: Instant,
    ): Outcome<Unit> = save { stored[barcode] = StoredLookup.Missing(checkedAt = checkedAt) }

    private fun save(block: () -> Unit): Outcome<Unit> {
        val failure = saveFailure
        if (failure != null) {
            return Outcome.Failure(failure)
        }
        block()
        return Outcome.Success(Unit)
    }
}
