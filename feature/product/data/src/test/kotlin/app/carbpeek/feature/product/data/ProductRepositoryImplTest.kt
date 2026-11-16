package app.carbpeek.feature.product.data

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.AppErrorException
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.database.product.ProductEntity
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.core.testing.FakeClock
import app.carbpeek.core.testing.TestAppDispatchers
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.openfoodapi.ProductNetworkSource
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ProductRepositoryImplTest {

    private val network = FakeNetworkSource()
    private val clock = FakeClock(NOW)

    @Test
    fun `when the row is missing then observe emits nothing and emits the product once it is saved`() = runTest {
        val persistence = FakePersistence()

        val emissions = collect(createRepository(persistence))
        assertTrue(emissions.isEmpty())

        persistence.rows.update { it + (BARCODE to product.toEntity(NOW)) }
        runCurrent()

        assertEquals(listOf(product), emissions)
    }

    @Test
    fun `when an equal row is emitted again then observe drops it and emits a changed row`() = runTest {
        val persistence = FakePersistence(storedProduct.toEntity(NOW))

        val emissions = collect(createRepository(persistence))
        persistence.rows.update { it + (BARCODE to storedProduct.toEntity(NOW)) }
        persistence.rows.update { it + (OTHER_BARCODE to product.copy(barcode = OTHER_BARCODE).toEntity(NOW)) }
        runCurrent()
        persistence.rows.update { it + (BARCODE to product.toEntity(NOW)) }
        runCurrent()

        assertEquals(listOf(storedProduct, product), emissions)
    }

    @Test
    fun `when observing then the network is never called`() = runTest {
        collect(createRepository(FakePersistence()))
        collect(createRepository(FakePersistence(storedProduct.toEntity(NOW - 31.days))))

        assertEquals(0, network.calls)
    }

    @Test
    fun `when observe throws then a storage error is thrown`() = runTest {
        val persistence = FakePersistence().apply { observeFailure = IllegalStateException("disk") }

        val exception = assertFailsWith<AppErrorException> {
            createRepository(persistence).observeProduct(BARCODE).toList()
        }

        assertEquals(AppError.Storage, exception.error)
    }

    @Test
    fun `when products are observed then the barcodes are passed and present rows are mapped`() = runTest {
        val persistence = FakePersistence(product.toEntity(NOW))
        val barcodes = setOf(Barcode(BARCODE), Barcode(OTHER_BARCODE))

        val products = createRepository(persistence).observeProducts(barcodes).first()

        assertEquals(listOf(product), products)
        assertEquals(listOf(listOf(BARCODE, OTHER_BARCODE)), persistence.observedBarcodes)
    }

    @Test
    fun `when observed products are equal then the emission is dropped and a changed one is passed`() = runTest {
        val persistence = FakePersistence(storedProduct.toEntity(NOW))
        val emissions = mutableListOf<List<Product>>()
        backgroundScope.launch {
            createRepository(persistence).observeProducts(setOf(Barcode(BARCODE))).toList(emissions)
        }
        runCurrent()

        persistence.rows.update { it + (OTHER_BARCODE to product.copy(barcode = OTHER_BARCODE).toEntity(NOW)) }
        runCurrent()
        persistence.rows.update { it + (BARCODE to product.toEntity(NOW)) }
        runCurrent()

        assertEquals(listOf(listOf(storedProduct), listOf(product)), emissions)
    }

    @Test
    fun `when observing products throws then a storage error is thrown`() = runTest {
        val persistence = FakePersistence().apply { observeFailure = IllegalStateException("disk") }

        val exception = assertFailsWith<AppErrorException> {
            createRepository(persistence).observeProducts(setOf(Barcode(BARCODE))).first()
        }

        assertEquals(AppError.Storage, exception.error)
    }

    @Test
    fun `when observing products is cancelled then cancellation is rethrown`() = runTest {
        val persistence = FakePersistence().apply { observeFailure = CancellationException("cancelled") }

        assertFailsWith<CancellationException> {
            createRepository(persistence).observeProducts(setOf(Barcode(BARCODE))).first()
        }
    }

    @Test
    fun `when the row is younger than the ttl then refresh succeeds without network and writes nothing`() = runTest {
        val fresh = storedProduct.toEntity(NOW - 29.days)
        val persistence = FakePersistence(fresh)

        val result = createRepository(persistence).refreshProduct(BARCODE)

        assertEquals(Outcome.Success(Unit), result)
        assertEquals(0, network.calls)
        assertEquals(fresh, persistence.rows.value[BARCODE])
        assertEquals(0, persistence.saves)
    }

    @Test
    fun `when the row is missing then refresh fetches saves and succeeds`() = runTest {
        val persistence = FakePersistence()

        val result = createRepository(persistence).refreshProduct(BARCODE)

        assertEquals(Outcome.Success(Unit), result)
        assertEquals(1, network.calls)
        assertEquals(product.toEntity(NOW), persistence.rows.value[BARCODE])
    }

    @Test
    fun `when the row is exactly the ttl old then refresh fetches and saves`() = runTest {
        assertStaleRowIsRefreshed(NOW - PRODUCT_CACHE_TTL)
        assertEquals(30.days, PRODUCT_CACHE_TTL)
    }

    @Test
    fun `when the row is dated after now then refresh fetches and saves`() = runTest {
        assertStaleRowIsRefreshed(NOW + 1.minutes)
    }

    @Test
    fun `when the row is stale and the fetch fails with network then refresh returns it and keeps the row`() = runTest {
        assertStaleRefreshFailure(AppError.Network)
    }

    @Test
    fun `when the row is stale and the fetch fails with parsing then refresh returns it and keeps the row`() = runTest {
        assertStaleRefreshFailure(AppError.Parsing)
    }

    @Test
    fun `when the row is stale and the fetch returns not found then refresh returns it and keeps the row`() = runTest {
        assertStaleRefreshFailure(AppError.NotFound)
    }

    @Test
    fun `when the row is missing and the fetch fails then refresh returns the failure and saves nothing`() = runTest {
        val persistence = FakePersistence()
        network.result = Outcome.Failure(AppError.Network)

        val result = createRepository(persistence).refreshProduct(BARCODE)

        assertEquals(Outcome.Failure(AppError.Network), result)
        assertTrue(persistence.rows.value.isEmpty())
        assertEquals(0, persistence.saves)
    }

    @Test
    fun `when saving fails then refresh returns storage failure`() = runTest {
        val persistence = FakePersistence().apply { saveFailure = IllegalStateException("disk") }

        val result = createRepository(persistence).refreshProduct(BARCODE)

        assertEquals(Outcome.Failure(AppError.Storage), result)
    }

    @Test
    fun `when find throws then refresh returns storage failure and the network is not called`() = runTest {
        val persistence = FakePersistence().apply { findFailure = IllegalStateException("disk") }

        val result = createRepository(persistence).refreshProduct(BARCODE)

        assertEquals(Outcome.Failure(AppError.Storage), result)
        assertEquals(0, network.calls)
    }

    @Test
    fun `when find throws cancellation then refresh throws cancellation`() = runTest {
        val persistence = FakePersistence().apply { findFailure = CancellationException("cancelled") }
        val repository = createRepository(persistence)

        assertFailsWith<CancellationException> { repository.refreshProduct(BARCODE) }
        assertEquals(0, network.calls)
    }

    @Test
    fun `when two refreshes run for one barcode then the network is called once and both get the same result`() =
        runTest {
            val repository = createRepository(FakePersistence())
            val gate = network.gate()

            val first = async { repository.refreshProduct(BARCODE) }
            val second = async { repository.refreshProduct(BARCODE) }
            runCurrent()
            gate.complete(Unit)

            assertEquals(Outcome.Success(Unit), first.await())
            assertEquals(Outcome.Success(Unit), second.await())
            assertEquals(1, network.calls)
        }

    @Test
    fun `when a refresh finished with a failure then the next refresh calls the network again`() = runTest {
        val repository = createRepository(FakePersistence())
        network.result = Outcome.Failure(AppError.Network)
        repository.refreshProduct(BARCODE)

        network.result = Outcome.Success(product)
        val result = repository.refreshProduct(BARCODE)

        assertEquals(Outcome.Success(Unit), result)
        assertEquals(2, network.calls)
    }

    @Test
    fun `when the caller is cancelled during the fetch then the fetch still completes and the row is saved`() =
        runTest {
            val persistence = FakePersistence()
            val gate = network.gate()
            val job = launch { createRepository(persistence).refreshProduct(BARCODE) }
            runCurrent()

            job.cancel()
            gate.complete(Unit)
            runCurrent()

            assertEquals(1, network.calls)
            assertEquals(product.toEntity(NOW), persistence.rows.value[BARCODE])
        }

    private suspend fun TestScope.assertStaleRowIsRefreshed(fetchedAt: Instant) {
        val persistence = FakePersistence(storedProduct.toEntity(fetchedAt))

        val result = createRepository(persistence).refreshProduct(BARCODE)

        assertEquals(Outcome.Success(Unit), result)
        assertEquals(1, network.calls)
        assertEquals(product.toEntity(NOW), persistence.rows.value[BARCODE])
    }

    private suspend fun TestScope.assertStaleRefreshFailure(error: AppError) {
        val stale = storedProduct.toEntity(NOW - 31.days)
        val persistence = FakePersistence(stale)
        network.result = Outcome.Failure(error)

        val result = createRepository(persistence).refreshProduct(BARCODE)

        assertEquals(Outcome.Failure(error), result)
        assertEquals(stale, persistence.rows.value[BARCODE])
        assertEquals(0, persistence.saves)
    }

    private fun TestScope.createRepository(persistence: FakePersistence) =
        ProductRepositoryImpl(persistence, network, clock, TestAppDispatchers(testScheduler), backgroundScope)

    private fun TestScope.collect(repository: ProductRepositoryImpl): List<Product> {
        val emissions = mutableListOf<Product>()
        backgroundScope.launch { repository.observeProduct(BARCODE).toList(emissions) }
        runCurrent()
        return emissions
    }

    private class FakePersistence(vararg initial: ProductEntity) : ProductPersistence {

        val rows = MutableStateFlow(initial.associateBy { it.barcode })
        val observedBarcodes = mutableListOf<List<String>>()
        var saves = 0
        var findFailure: Throwable? = null
        var saveFailure: Throwable? = null
        var observeFailure: Throwable? = null

        override suspend fun find(barcode: String): ProductEntity? {
            findFailure?.let { throw it }
            return rows.value[barcode]
        }

        override fun observe(barcode: String): Flow<ProductEntity?> {
            val failure = observeFailure ?: return rows.map { it[barcode] }
            return flow { throw failure }
        }

        override fun observe(barcodes: List<String>): Flow<List<ProductEntity>> {
            observedBarcodes += listOf(barcodes)
            val failure = observeFailure ?: return rows.map { row -> barcodes.mapNotNull(row::get) }
            return flow { throw failure }
        }

        override suspend fun save(product: ProductEntity) {
            saveFailure?.let { throw it }
            saves++
            rows.update { it + (product.barcode to product) }
        }
    }

    private class FakeNetworkSource : ProductNetworkSource {

        var result: Outcome<Product> = Outcome.Success(product)
        var calls = 0
        private var gate: CompletableDeferred<Unit>? = null

        fun gate(): CompletableDeferred<Unit> = CompletableDeferred<Unit>().also { gate = it }

        override suspend fun getProduct(barcode: String): Outcome<Product> {
            calls++
            gate?.await()
            return result
        }
    }

    private companion object {
        const val BARCODE = "3017620422003"
        const val OTHER_BARCODE = "5900259000002"

        val NOW = Instant.fromEpochMilliseconds(1_700_000_000_000)

        val product = Product(
            name = "Nutella",
            barcode = BARCODE,
            brand = "Ferrero",
            nutriments = ProductNutriments(80.0, 539.0, 4.6, 30.9, 0.9, 6.3, 8.6, 57.5, 0.02, 0.107),
            imageUrl = "https://images.openfoodfacts.org/front.jpg",
            servingAmount = "15",
            category = ProductCategory.SPREADS,
        )

        val storedProduct = product.copy(name = "Nutella (old)")
    }
}
