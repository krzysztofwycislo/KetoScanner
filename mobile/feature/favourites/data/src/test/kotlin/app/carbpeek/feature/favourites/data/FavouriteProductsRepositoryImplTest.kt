package app.carbpeek.feature.favourites.data

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.AppErrorException
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.getOrThrow
import app.carbpeek.core.database.favourite.FavouriteProductEntity
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.core.testing.TestAppDispatchers
import app.carbpeek.feature.analysis.api.AnalyzeProducts
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.favourites.api.model.FavouriteProduct
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
import kotlin.time.Instant

class FavouriteProductsRepositoryImplTest {

    private val analyzeProducts = FakeAnalyzeProducts()

    @Test
    fun `when persistence emits entities then they are mapped with their analysis and query is passed`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE), entity(OTHER_BARCODE)))
        val analyses = mapOf(Barcode(BARCODE) to rated(DietRate.GOOD))
        analyzeProducts.results.value = Outcome.Success(analyses)

        val favourites = createRepository(persistence).observeFavourites(QUERY).first()

        assertEquals(listOf(entity(BARCODE), entity(OTHER_BARCODE)).toDomain(analyses), favourites)
        assertEquals(listOf(QUERY), persistence.observedQueries)
        assertEquals(listOf(setOf(Barcode(BARCODE), Barcode(OTHER_BARCODE))), analyzeProducts.requests)
    }

    @Test
    fun `when a new analysis is emitted then the favourites are emitted again with the new rate`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE)))
        analyzeProducts.results.value = Outcome.Success(mapOf(Barcode(BARCODE) to rated(DietRate.GOOD)))
        val emissions = collect(createRepository(persistence))

        analyzeProducts.results.value = Outcome.Success(mapOf(Barcode(BARCODE) to rated(DietRate.NOT_ADVISED)))
        runCurrent()

        assertEquals(
            listOf(DietRate.GOOD, DietRate.NOT_ADVISED),
            emissions.map { favourites -> favourites.single().dietRate },
        )
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

        val favourite = collect(createRepository(persistence)).single().single()

        assertNull(favourite.dietRate)
        assertNull(favourite.carbsPer100g)
        assertEquals(ProductCategory.YOGURT_AND_CREAM, favourite.category)
    }

    @Test
    fun `when analysis is insufficient data then the favourite has a null rate and the product carbs`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE)))
        analyzeProducts.results.value = Outcome.Success(
            mapOf(Barcode(BARCODE) to ProductAnalysis.InsufficientData(product)),
        )

        val favourite = collect(createRepository(persistence)).single().single()

        assertNull(favourite.dietRate)
        assertEquals(CARBS, favourite.carbsPer100g)
    }

    @Test
    fun `when the product category changes then it replaces the stored category`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE, category = "YOGURT_AND_CREAM")))
        analyzeProducts.results.value = Outcome.Success(mapOf(Barcode(BARCODE) to rated(DietRate.GOOD)))

        val favourite = collect(createRepository(persistence)).single().single()

        assertEquals(ProductCategory.SPREADS, favourite.category)
    }

    @Test
    fun `when analysis fails then the same error passes through`() = runTest {
        val persistence = FakePersistence(listOf(entity(BARCODE)))
        analyzeProducts.results.value = Outcome.Failure(AppError.Parsing)

        val exception = assertFailsWith<AppErrorException> {
            createRepository(persistence).observeFavourites(QUERY).first()
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
    fun `when persistence flow throws then a storage error is thrown`() = runTest {
        val repository = createRepository(FakePersistence(failure = IllegalStateException("disk")))

        val exception = assertFailsWith<AppErrorException> { repository.observeFavourites(QUERY).first() }

        assertEquals(AppError.Storage, exception.error)
    }

    @Test
    fun `when persistence flow is cancelled then cancellation is rethrown`() = runTest {
        val repository = createRepository(FakePersistence(failure = CancellationException("cancelled")))

        assertFailsWith<CancellationException> { repository.observeFavourites(QUERY).first() }
    }

    @Test
    fun `when product is added then mapped entity is inserted and success is returned`() = runTest {
        val persistence = FakePersistence()

        assertEquals(Outcome.Success(Unit), createRepository(persistence).addToFavourites(product))
        assertEquals(listOf(product.toFavouriteEntity()), persistence.inserted)
    }

    @Test
    fun `when product is removed then mapped entity is deleted and success is returned`() = runTest {
        val persistence = FakePersistence()

        assertEquals(Outcome.Success(Unit), createRepository(persistence).removeFromFavourites(product))
        assertEquals(listOf(product.toFavouriteEntity()), persistence.deleted)
    }

    @Test
    fun `when a favourite row exists then isFavourite is true and the barcode is passed through`() = runTest {
        val persistence = FakePersistence(found = entity(BARCODE))

        assertEquals(Outcome.Success(true), createRepository(persistence).isFavourite(Barcode(BARCODE)))
        assertEquals(listOf(BARCODE), persistence.foundBarcodes)
    }

    @Test
    fun `when no favourite row exists then isFavourite is false`() = runTest {
        val repository = createRepository(FakePersistence())

        assertEquals(Outcome.Success(false), repository.isFavourite(Barcode(BARCODE)))
    }

    @Test
    fun `when persistence throws on add remove or lookup then storage failure is returned`() = runTest {
        val repository = createRepository(FakePersistence(failure = IllegalStateException("disk")))

        assertEquals(Outcome.Failure(AppError.Storage), repository.addToFavourites(product))
        assertEquals(Outcome.Failure(AppError.Storage), repository.removeFromFavourites(product))
        assertEquals(Outcome.Failure(AppError.Storage), repository.isFavourite(Barcode(BARCODE)))
    }

    @Test
    fun `when persistence is cancelled on add remove or lookup then cancellation is rethrown`() = runTest {
        val repository = createRepository(FakePersistence(failure = CancellationException("cancelled")))

        assertFailsWith<CancellationException> { repository.addToFavourites(product) }
        assertFailsWith<CancellationException> { repository.removeFromFavourites(product) }
        assertFailsWith<CancellationException> { repository.isFavourite(Barcode(BARCODE)) }
    }

    private fun TestScope.createRepository(persistence: FakePersistence) =
        FavouriteProductsRepositoryImpl(persistence, analyzeProducts, TestAppDispatchers(testScheduler))

    private fun TestScope.collect(repository: FavouriteProductsRepositoryImpl): List<List<FavouriteProduct>> {
        val emissions = mutableListOf<List<FavouriteProduct>>()
        backgroundScope.launch { repository.observeFavourites(QUERY).toList(emissions) }
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
        initial: List<FavouriteProductEntity> = emptyList(),
        private val found: FavouriteProductEntity? = null,
        private val failure: Throwable? = null,
    ) : FavouritesPersistence {

        private val entities = MutableSharedFlow<List<FavouriteProductEntity>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        ).apply { tryEmit(initial) }

        val observedQueries = mutableListOf<String>()
        val inserted = mutableListOf<FavouriteProductEntity>()
        val deleted = mutableListOf<FavouriteProductEntity>()
        val foundBarcodes = mutableListOf<String>()

        fun emit(list: List<FavouriteProductEntity>) {
            entities.tryEmit(list)
        }

        override fun observe(nameQuery: String): Flow<List<FavouriteProductEntity>> = flow {
            failure?.let { throw it }
            observedQueries += nameQuery
            emitAll(entities)
        }

        override suspend fun insert(entity: FavouriteProductEntity) {
            failure?.let { throw it }
            inserted += entity
        }

        override suspend fun delete(entity: FavouriteProductEntity) {
            failure?.let { throw it }
            deleted += entity
        }

        override suspend fun findByBarcode(barcode: String): FavouriteProductEntity? {
            failure?.let { throw it }
            foundBarcodes += barcode
            return found
        }
    }

    private companion object {
        const val QUERY = "butter"
        const val BARCODE = "5900000000001"
        const val OTHER_BARCODE = "5900000000002"
        const val CARBS = 12.5

        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)

        fun entity(barcode: String, category: String? = null) =
            FavouriteProductEntity(barcode, "Name $barcode", "Brand", null, category).apply {
                updateTime = UPDATE_TIME
            }

        val product = Product(
            name = "Peanut butter",
            barcode = BARCODE,
            brand = "Nuts & Co",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, CARBS, null, null),
            imageUrl = "https://example.com/image.jpg",
            servingAmount = null,
            category = ProductCategory.SPREADS,
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
