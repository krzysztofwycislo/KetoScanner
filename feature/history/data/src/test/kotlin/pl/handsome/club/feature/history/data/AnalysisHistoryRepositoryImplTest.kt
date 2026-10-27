package pl.handsome.club.feature.history.data

import kotlinx.coroutines.test.runTest
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.database.history.ProductAnalysisHistoryEntryEntity
import pl.handsome.club.core.testing.TestAppDispatchers
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.time.Instant

class AnalysisHistoryRepositoryImplTest {

    @Test
    fun `when persistence returns entries then mapped entries are returned and amount is passed through`() = runTest {
        val persistence = FakePersistence(entries = listOf(entity("1"), entity("2")))
        val repository = AnalysisHistoryRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        val outcome = repository.getLastUniqueEntries(AMOUNT)

        val success = assertIs<Outcome.Success<*>>(outcome)
        assertEquals(listOf(entity("1"), entity("2")).toDomain(), success.value)
        assertEquals(listOf(AMOUNT), persistence.requestedAmounts)
    }

    @Test
    fun `when persistence throws on read then storage failure is returned`() = runTest {
        val persistence = FakePersistence(failure = IllegalStateException("disk"))
        val repository = AnalysisHistoryRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Failure(AppError.Storage), repository.getLastUniqueEntries(AMOUNT))
    }

    @Test
    fun `when persistence throws on insert then save returns storage failure`() = runTest {
        val persistence = FakePersistence(failure = IllegalStateException("disk"))
        val repository = AnalysisHistoryRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Failure(AppError.Storage), repository.save(result))
    }

    @Test
    fun `when result is saved then mapped entity is inserted and success is returned`() = runTest {
        val persistence = FakePersistence()
        val repository = AnalysisHistoryRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        val outcome = repository.save(result)

        assertEquals(Outcome.Success(Unit), outcome)
        assertEquals(listOf(result.toEntity()), persistence.inserted)
    }

    @Test
    fun `when persistence is cancelled then cancellation is rethrown`() = runTest {
        val persistence = FakePersistence(failure = CancellationException("cancelled"))
        val repository = AnalysisHistoryRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertFailsWith<CancellationException> { repository.getLastUniqueEntries(AMOUNT) }
        assertFailsWith<CancellationException> { repository.save(result) }
    }

    private class FakePersistence(
        private val entries: List<ProductAnalysisHistoryEntryEntity> = emptyList(),
        private val failure: Throwable? = null,
    ) : AnalysisHistoryPersistence {

        val inserted = mutableListOf<ProductAnalysisHistoryEntryEntity>()
        val requestedAmounts = mutableListOf<Int>()

        override suspend fun insert(entity: ProductAnalysisHistoryEntryEntity) {
            failure?.let { throw it }
            inserted += entity
        }

        override suspend fun lastUniqueEntries(amount: Int): List<ProductAnalysisHistoryEntryEntity> {
            failure?.let { throw it }
            requestedAmounts += amount
            return entries
        }
    }

    private companion object {
        const val AMOUNT = 10

        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)

        fun entity(barcode: String) = ProductAnalysisHistoryEntryEntity("Name $barcode", barcode, "Brand", null).apply {
            updateTime = UPDATE_TIME
        }

        val result = ProductAnalysis.InsufficientData(
            product = Product(
                name = "Peanut butter",
                barcode = "5900000000001",
                brand = "Nuts & Co",
                nutriments = ProductNutriments(null, null, null, null, null, null, null, null, null, null),
                imageUrl = null,
                servingAmount = null,
            ),
        )
    }
}
