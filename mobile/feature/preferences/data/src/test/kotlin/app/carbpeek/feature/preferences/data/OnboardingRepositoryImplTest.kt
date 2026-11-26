package app.carbpeek.feature.preferences.data

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.testing.TestAppDispatchers
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.preferences.api.model.OnboardingTip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
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
    fun `when no diet is stored then setup is done`() = runTest {
        val repository = createRepository(
            persistence = FakePersistence(
                stored = StoredDietPreferences(maxDailyCarbsAmount = null, dietMode = "NONE"),
            ),
        )

        assertEquals(Outcome.Success(true), repository.isSetupDone())
    }

    @Test
    fun `when only the details setting is stored then setup is not done`() = runTest {
        val repository = createRepository(
            persistence = FakePersistence(
                stored = StoredDietPreferences(
                    maxDailyCarbsAmount = null,
                    dietMode = null,
                    opensDetailsRightAway = true,
                ),
            ),
        )

        assertEquals(Outcome.Success(false), repository.isSetupDone())
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
    fun `when setup is completed with each preset then its mode name is saved`() = runTest {
        val persistence = FakePersistence()
        val repository = createRepository(persistence = persistence)

        DietPreset.entries.forEach { repository.completeSetup(preset = it) }

        assertEquals(listOf(20 to "KETO", 40 to "KETO", 100 to "LOW_CARB"), persistence.completed)
    }

    @Test
    fun `when completing setup throws then storage failure is returned`() = runTest {
        val repository = createRepository(persistence = FakePersistence(failure = IllegalStateException("disk")))

        assertEquals(Outcome.Failure(AppError.Storage), repository.completeSetup(preset = DietPreset.KETO))
    }

    @Test
    fun `when tips are pending then they are returned`() = runTest {
        val tips = setOf(OnboardingTip.FIRST_SCAN_HINT, OnboardingTip.VERDICT_TIP)
        val repository = createRepository(
            persistence = FakePersistence(
                stored = StoredDietPreferences(maxDailyCarbsAmount = 20, dietMode = "KETO", pendingTips = tips),
            ),
        )

        assertEquals(Outcome.Success(tips), repository.pendingTips())
    }

    @Test
    fun `when store is empty then no tips are pending`() = runTest {
        val repository = createRepository(persistence = FakePersistence())

        assertEquals(Outcome.Success(emptySet()), repository.pendingTips())
    }

    @Test
    fun `when reading pending tips throws then storage failure is returned`() = runTest {
        val repository = createRepository(persistence = FakePersistence(failure = IllegalStateException("disk")))

        assertEquals(Outcome.Failure(AppError.Storage), repository.pendingTips())
    }

    @Test
    fun `when a tip is marked shown then it is forwarded and success is returned`() = runTest {
        val persistence = FakePersistence()
        val repository = createRepository(persistence = persistence)

        assertEquals(Outcome.Success(Unit), repository.markTipShown(tip = OnboardingTip.VERDICT_TIP))
        assertEquals(listOf(OnboardingTip.VERDICT_TIP), persistence.shownTips)
    }

    @Test
    fun `when marking a tip shown throws then storage failure is returned`() = runTest {
        val repository = createRepository(persistence = FakePersistence(failure = IllegalStateException("disk")))

        assertEquals(
            Outcome.Failure(AppError.Storage),
            repository.markTipShown(tip = OnboardingTip.FIRST_SCAN_HINT),
        )
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
        val shownTips = mutableListOf<OnboardingTip>()

        override fun observe(): Flow<StoredDietPreferences> {
            val failure = failure ?: return flowOf(stored)
            return flow { throw failure }
        }

        override suspend fun save(settings: StoredProfileSettings) {
            failure?.let { throw it }
        }

        override suspend fun completeSetup(maxDailyCarbsAmount: Int, dietMode: String) {
            failure?.let { throw it }
            completed += maxDailyCarbsAmount to dietMode
        }

        override suspend fun markTipShown(tip: OnboardingTip) {
            failure?.let { throw it }
            shownTips += tip
        }
    }
}
