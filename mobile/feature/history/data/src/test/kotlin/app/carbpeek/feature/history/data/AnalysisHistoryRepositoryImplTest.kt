package app.carbpeek.feature.history.data

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.AppErrorException
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.getOrThrow
import app.carbpeek.core.database.history.ProductAnalysisHistoryEntryEntity
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.core.testing.TestAppDispatchers
import app.carbpeek.feature.analysis.api.AnalyzeProducts
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class AnalysisHistoryRepositoryImplTest {

    private val analyzeProducts = FakeAnalyzeProducts()

    @Test
    fun `when persistence throws on insert then save returns storage failure`() = runTest {
        val repository = createRepository(FakePersistence(failure = IllegalStateException("disk")))

        assertEquals(Outcome.Failure(AppError.Storage), repository.save(product))
    }

    @Test
    fun `when product is saved then mapped entity is inserted and success is returned`() = runTest {
        val persistence = FakePersistence()

        val outcome = createRepository(persistence).save(product)

        assertEquals(Outcome.Success(Unit), outcome)
        assertEquals(listOf(product.toEntity()), persistence.inserted)
    }

    @Test
    fun `when persistence is cancelled then cancellation is rethrown`() = runTest {
        val repository = createRepository(FakePersistence(failure = CancellationException("cancelled")))

        assertFailsWith<CancellationException> { repository.save(product) }
    }

    @Test
    fun `when persistence emits entries then they are mapped with their analysis and the amount is passed`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE), entity(OTHER_BARCODE)))
        val analyses = mapOf(Barcode(BARCODE) to rated(DietRate.GOOD))
        analyzeProducts.results.value = Outcome.Success(analyses)

        val entries = createRepository(persistence).observeLastUniqueEntries(AMOUNT).first()

        assertEquals(listOf(entity(BARCODE), entity(OTHER_BARCODE)).toDomain(analyses), entries)
        assertEquals(listOf(AMOUNT), persistence.requestedAmounts)
        assertEquals(listOf(setOf(Barcode(BARCODE), Barcode(OTHER_BARCODE))), analyzeProducts.requests)
    }

    @Test
    fun `when a new analysis is emitted then the entries are emitted again with the new rate`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE)))
        analyzeProducts.results.value = Outcome.Success(mapOf(Barcode(BARCODE) to rated(DietRate.GOOD)))
        val emissions = collect(createRepository(persistence))

        analyzeProducts.results.value = Outcome.Success(mapOf(Barcode(BARCODE) to rated(DietRate.NOT_ADVISED)))
        runCurrent()

        assertEquals(listOf(DietRate.GOOD, DietRate.NOT_ADVISED), emissions.map { it.single().dietRate })
    }

    @Test
    fun `when a new entity list is emitted then the new barcode set is requested`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE)))
        collect(createRepository(persistence))

        persistence.emit(listOf(entity(OTHER_BARCODE), entity(BARCODE)))
        runCurrent()

        assertEquals(
            listOf(setOf(Barcode(BARCODE)), setOf(Barcode(OTHER_BARCODE), Barcode(BARCODE))),
            analyzeProducts.requests,
        )
    }

    @Test
    fun `when a row has no analysis then it has a null rate null carbs and its stored category`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE, category = "YOGURT_AND_CREAM")))

        val entry = collect(createRepository(persistence)).single().single()

        assertNull(entry.dietRate)
        assertNull(entry.carbsPer100g)
        assertEquals(ProductCategory.YOGURT_AND_CREAM, entry.category)
    }

    @Test
    fun `when analysis is insufficient data then the entry has a null rate and the product carbs`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE)))
        analyzeProducts.results.value = Outcome.Success(
            mapOf(Barcode(BARCODE) to ProductAnalysis.InsufficientData(product)),
        )

        val entry = collect(createRepository(persistence)).single().single()

        assertNull(entry.dietRate)
        assertEquals(CARBS, entry.carbsPer100g)
    }

    @Test
    fun `when analysis fails then the same error passes through`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE)))
        analyzeProducts.results.value = Outcome.Failure(AppError.Parsing)

        val exception = assertFailsWith<AppErrorException> {
            createRepository(persistence).observeLastUniqueEntries(AMOUNT).first()
        }

        assertEquals(AppError.Parsing, exception.error)
    }

    @Test
    fun `when the result is equal then it is not emitted again`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE)))
        analyzeProducts.results.value = Outcome.Success(mapOf(Barcode(BARCODE) to rated(DietRate.GOOD)))
        val emissions = collect(createRepository(persistence))

        analyzeProducts.results.value = Outcome.Success(
            mapOf(Barcode(BARCODE) to rated(DietRate.GOOD), Barcode(OTHER_BARCODE) to rated(DietRate.NEUTRAL)),
        )
        runCurrent()
        persistence.emit(listOf(entity(BARCODE)))
        runCurrent()

        assertEquals(1, emissions.size)
    }

    @Test
    fun `when the same product is scanned again then the new update time is emitted`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE)))
        val emissions = collect(createRepository(persistence))
        val later = UPDATE_TIME + 1.hours

        persistence.emit(listOf(entity(BARCODE).apply { updateTime = later }))
        runCurrent()

        assertEquals(listOf(UPDATE_TIME, later), emissions.map { it.single().updateTime })
    }

    @Test
    fun `when observed persistence throws then a storage error is thrown`() = runTest {
        val repository = createRepository(FakePersistence(failure = IllegalStateException("disk")))

        val exception = assertFailsWith<AppErrorException> { repository.observeLastUniqueEntries(AMOUNT).first() }

        assertEquals(AppError.Storage, exception.error)
    }

    @Test
    fun `when observed persistence is cancelled then cancellation is rethrown`() = runTest {
        val repository = createRepository(FakePersistence(failure = CancellationException("cancelled")))

        assertFailsWith<CancellationException> { repository.observeLastUniqueEntries(AMOUNT).first() }
    }

    private fun TestScope.createRepository(persistence: FakePersistence) =
        AnalysisHistoryRepositoryImpl(persistence, analyzeProducts, TestAppDispatchers(testScheduler))

    private fun TestScope.collect(
        repository: AnalysisHistoryRepositoryImpl,
    ): List<List<ProductAnalysisHistoryEntry>> {
        val emissions = mutableListOf<List<ProductAnalysisHistoryEntry>>()
        backgroundScope.launch { repository.observeLastUniqueEntries(AMOUNT).toList(emissions) }
        runCurrent()
        return emissions
    }

    private class FakeAnalyzeProducts : AnalyzeProducts {

        val results = MutableStateFlow<Outcome<Map<Barcode, ProductAnalysis>>>(Outcome.Success(emptyMap()))
        val requests = mutableListOf<Set<Barcode>>()

        override fun invoke(barcodes: Set<Barcode>): Flow<Map<Barcode, ProductAnalysis>> = flow {
            requests += barcodes
            emitAll(results.map { it.getOrThrow() })
        }
    }

    private class FakePersistence(
        initial: List<ProductAnalysisHistoryEntryEntity> = emptyList(),
        private val failure: Throwable? = null,
    ) : AnalysisHistoryPersistence {

        private val entries = MutableSharedFlow<List<ProductAnalysisHistoryEntryEntity>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        ).apply { tryEmit(initial) }

        val inserted = mutableListOf<ProductAnalysisHistoryEntryEntity>()
        val requestedAmounts = mutableListOf<Int>()

        fun emit(list: List<ProductAnalysisHistoryEntryEntity>) {
            entries.tryEmit(list)
        }

        override suspend fun insert(entity: ProductAnalysisHistoryEntryEntity) {
            failure?.let { throw it }
            inserted += entity
        }

        override fun observeLastUniqueEntries(amount: Int): Flow<List<ProductAnalysisHistoryEntryEntity>> = flow {
            failure?.let { throw it }
            requestedAmounts += amount
            emitAll(entries)
        }
    }

    private companion object {
        const val AMOUNT = 10
        const val BARCODE = "5900000000001"
        const val OTHER_BARCODE = "5900000000002"
        const val CARBS = 12.5

        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)

        fun entity(barcode: String, category: String? = null) =
            ProductAnalysisHistoryEntryEntity("Name $barcode", barcode, "Brand", category).apply {
                updateTime = UPDATE_TIME
            }

        val product = Product(
            name = "Peanut butter",
            barcode = BARCODE,
            brand = "Nuts & Co",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, CARBS, null, null),
            imageUrl = null,
            servingAmount = null,
            category = ProductCategory.OTHER,
        )

        fun rated(rate: DietRate) = ProductAnalysis.Rated(
            product,
            MacronutrientAnalysisResult(
                dietMode = DietMode.KETO,
                rate = rate,
                reasons = emptyList(),
                carbsPer100g = CARBS,
                dailyCarbConsumption = 40,
            ),
        )
    }
}
