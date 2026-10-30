package pl.handsome.club.feature.favourites.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.database.favourite.FavouriteProductEntity
import pl.handsome.club.core.testing.TestAppDispatchers
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class FavouriteProductsRepositoryImplTest {

    @Test
    fun `when persistence emits entities then mapped favourites are emitted and query is passed through`() = runTest {
        val persistence = FakePersistence(entities = listOf(entity("1"), entity("2")))
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        val emitted = repository.observeFavourites(QUERY).toList()

        assertEquals(listOf(Outcome.Success(listOf(entity("1"), entity("2")).toDomain())), emitted)
        assertEquals(listOf(QUERY), persistence.observedQueries)
    }

    @Test
    fun `when persistence flow throws then storage failure is emitted and flow completes`() = runTest {
        val persistence = FakePersistence(failure = IllegalStateException("disk"))
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertEquals(listOf(Outcome.Failure(AppError.Storage)), repository.observeFavourites(QUERY).toList())
    }

    @Test
    fun `when persistence flow is cancelled then cancellation is rethrown`() = runTest {
        val persistence = FakePersistence(failure = CancellationException("cancelled"))
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertFailsWith<CancellationException> { repository.observeFavourites(QUERY).toList() }
    }

    @Test
    fun `when analysis is added then mapped entity is inserted and success is returned`() = runTest {
        val persistence = FakePersistence()
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Success(Unit), repository.addToFavourites(analysis))
        assertEquals(listOf(analysis.toFavouriteEntity()), persistence.inserted)
    }

    @Test
    fun `when rating is updated then barcode rate name and carbs are passed and success is returned`() = runTest {
        val persistence = FakePersistence()
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Success(Unit), repository.updateRating(analysis))
        assertEquals(listOf(UpdatedRating(BARCODE, "GOOD", CARBS)), persistence.updatedRatings)
    }

    @Test
    fun `when rating of insufficient data is updated then a null rate and the product carbs are passed`() = runTest {
        val persistence = FakePersistence()
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        repository.updateRating(ProductAnalysis.InsufficientData(product))

        assertEquals(listOf(UpdatedRating(BARCODE, null, CARBS)), persistence.updatedRatings)
    }

    @Test
    fun `when product is removed then mapped entity is deleted and success is returned`() = runTest {
        val persistence = FakePersistence()
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Success(Unit), repository.removeFromFavourites(product))
        assertEquals(listOf(product.toFavouriteEntity()), persistence.deleted)
    }

    @Test
    fun `when favourite is found by barcode then it is mapped and barcode is passed through`() = runTest {
        val persistence = FakePersistence(found = entity(BARCODE))
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Success(entity(BARCODE).toDomain()), repository.findByBarcode(BARCODE))
        assertEquals(listOf(BARCODE), persistence.foundBarcodes)
    }

    @Test
    fun `when favourite is not found by barcode then success with null is returned`() = runTest {
        val repository = FavouriteProductsRepositoryImpl(FakePersistence(), TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Success(null), repository.findByBarcode(BARCODE))
    }

    @Test
    fun `when persistence throws on add remove find or rating update then storage failure is returned`() = runTest {
        val persistence = FakePersistence(failure = IllegalStateException("disk"))
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Failure(AppError.Storage), repository.addToFavourites(analysis))
        assertEquals(Outcome.Failure(AppError.Storage), repository.removeFromFavourites(product))
        assertEquals(Outcome.Failure(AppError.Storage), repository.findByBarcode(BARCODE))
        assertEquals(Outcome.Failure(AppError.Storage), repository.updateRating(analysis))
    }

    @Test
    fun `when persistence is cancelled on add remove find or rating update then cancellation is rethrown`() = runTest {
        val persistence = FakePersistence(failure = CancellationException("cancelled"))
        val repository = FavouriteProductsRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertFailsWith<CancellationException> { repository.addToFavourites(analysis) }
        assertFailsWith<CancellationException> { repository.removeFromFavourites(product) }
        assertFailsWith<CancellationException> { repository.findByBarcode(BARCODE) }
        assertFailsWith<CancellationException> { repository.updateRating(analysis) }
    }

    private data class UpdatedRating(val barcode: String, val dietRate: String?, val carbsPer100g: Double?)

    private class FakePersistence(
        private val entities: List<FavouriteProductEntity> = emptyList(),
        private val found: FavouriteProductEntity? = null,
        private val failure: Throwable? = null,
    ) : FavouritesPersistence {

        val observedQueries = mutableListOf<String>()
        val inserted = mutableListOf<FavouriteProductEntity>()
        val deleted = mutableListOf<FavouriteProductEntity>()
        val foundBarcodes = mutableListOf<String>()
        val updatedRatings = mutableListOf<UpdatedRating>()

        override fun observe(nameQuery: String): Flow<List<FavouriteProductEntity>> {
            observedQueries += nameQuery
            val failure = failure ?: return flowOf(entities)
            return flow { throw failure }
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

        override suspend fun updateRating(barcode: String, dietRate: String?, carbsPer100g: Double?) {
            failure?.let { throw it }
            updatedRatings += UpdatedRating(barcode, dietRate, carbsPer100g)
        }
    }

    private companion object {
        const val QUERY = "butter"
        const val BARCODE = "5900000000001"
        const val CARBS = 12.5

        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)

        fun entity(barcode: String) =
            FavouriteProductEntity(barcode, "Name $barcode", "Brand", null, null, null).apply {
                updateTime = UPDATE_TIME
            }

        val product = Product(
            name = "Peanut butter",
            barcode = BARCODE,
            brand = "Nuts & Co",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, CARBS, null, null),
            imageUrl = "https://example.com/image.jpg",
            servingAmount = null,
        )

        val analysis = ProductAnalysis.Rated(
            product,
            MacronutrientAnalysisResult(DietMode.KETO, DietRate.GOOD, emptyList(), CARBS, 800.0, 40),
        )
    }
}
