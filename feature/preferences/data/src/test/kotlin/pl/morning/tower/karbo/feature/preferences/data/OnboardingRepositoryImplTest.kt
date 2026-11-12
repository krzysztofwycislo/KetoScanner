package pl.morning.tower.karbo.feature.preferences.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.testing.TestAppDispatchers
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingRepositoryImplTest {

    @Test
    fun `when store is empty then setup is not done`() = runTest {
        val repository = createRepository(
            persistence = FakePersistence(
                stored = StoredDietPreferences(maxDailyCarbsAmount = null, dietMode = null),
            ),
        )

        assertEquals(Outcome.Success(false), repository.isSetupDone())
    }

    @Test
    fun `when the setup flag is stored then setup is done`() = runTest {
        val repository = createRepository(
            persistence = FakePersistence(
                stored = StoredDietPreferences(maxDailyCarbsAmount = null, dietMode = null, isSetupDone = true),
            ),
        )

        assertEquals(Outcome.Success(true), repository.isSetupDone())
    }

    @Test
    fun `when only an amount is stored then setup is done`() = runTest {
        val repository = createRepository(
            persistence = FakePersistence(
                stored = StoredDietPreferences(maxDailyCarbsAmount = 25, dietMode = null),
            ),
        )

        assertEquals(Outcome.Success(true), repository.isSetupDone())
    }

    @Test
    fun `when only a mode is stored then setup is done`() = runTest {
        val repository = createRepository(
            persistence = FakePersistence(
                stored = StoredDietPreferences(maxDailyCarbsAmount = null, dietMode = "LOW_CARB"),
            ),
        )

        assertEquals(Outcome.Success(true), repository.isSetupDone())
    }

    @Test
    fun `when reading throws then storage failure is returned`() = runTest {
        val repository = createRepository(persistence = FakePersistence(failure = IllegalStateException("disk")))

        assertEquals(Outcome.Failure(AppError.Storage), repository.isSetupDone())
    }

    @Test
    fun `when setup is completed then the preset amount and mode name are saved and success is returned`() =
        runTest {
            val persistence = FakePersistence()
            val repository = createRepository(persistence = persistence)

            assertEquals(Outcome.Success(Unit), repository.completeSetup(preset = DietPreset.LOW_CARB))
            assertEquals(listOf(100 to "LOW_CARB"), persistence.completed)
        }

    @Test
    fun `when completing setup throws then storage failure is returned`() = runTest {
        val repository = createRepository(persistence = FakePersistence(failure = IllegalStateException("disk")))

        assertEquals(Outcome.Failure(AppError.Storage), repository.completeSetup(preset = DietPreset.KETO))
    }

    private fun TestScope.createRepository(persistence: FakePersistence) = OnboardingRepositoryImpl(
        persistence = persistence,
        dispatchers = TestAppDispatchers(testScheduler),
    )

    private class FakePersistence(
        private val stored: StoredDietPreferences = StoredDietPreferences(maxDailyCarbsAmount = null, dietMode = null),
        private val failure: Throwable? = null,
    ) : DietPreferencesPersistence {

        val completed = mutableListOf<Pair<Int, String>>()

        override fun observe(): Flow<StoredDietPreferences> {
            val failure = failure ?: return flowOf(stored)
            return flow { throw failure }
        }

        override suspend fun save(maxDailyCarbsAmount: Int, dietMode: String) {
            failure?.let { throw it }
        }

        override suspend fun completeSetup(maxDailyCarbsAmount: Int, dietMode: String) {
            failure?.let { throw it }
            completed += maxDailyCarbsAmount to dietMode
        }
    }
}
