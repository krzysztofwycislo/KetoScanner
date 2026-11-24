package app.carbpeek.feature.preferences.data

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.AppErrorException
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.testing.TestAppDispatchers
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.preferences.api.model.OnboardingTip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DietPreferencesRepositoryImplTest {

    @Test
    fun `when no amount is stored then default preferences are returned`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(amounts = listOf(null)),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(Outcome.Success(DietPreferences.Default), repository.getDietPreferences())
    }

    @Test
    fun `when an amount is stored then it is mapped to preferences`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(amounts = listOf(20)),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(Outcome.Success(DietPreset.STRICT_KETO.preferences), repository.getDietPreferences())
    }

    @Test
    fun `when no mode is stored then mode is keto`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(stored = listOf(StoredDietPreferences(maxDailyCarbsAmount = 20, dietMode = null))),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(Outcome.Success(DietPreset.STRICT_KETO.preferences), repository.getDietPreferences())
    }

    @Test
    fun `when an unknown mode is stored then mode is keto`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(
                stored = listOf(StoredDietPreferences(maxDailyCarbsAmount = 20, dietMode = "PALEO")),
            ),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(Outcome.Success(DietPreset.STRICT_KETO.preferences), repository.getDietPreferences())
    }

    @Test
    fun `when low carb mode is stored then it is mapped to preferences`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(
                stored = listOf(StoredDietPreferences(maxDailyCarbsAmount = 100, dietMode = "LOW_CARB")),
            ),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(Outcome.Success(DietPreset.LOW_CARB.preferences), repository.getDietPreferences())
    }

    @Test
    fun `when the stored amount and mode match no preset then keto is returned and nothing is saved`() = runTest {
        listOf(
            StoredDietPreferences(maxDailyCarbsAmount = 35, dietMode = "KETO"),
            StoredDietPreferences(maxDailyCarbsAmount = 999, dietMode = "KETO"),
            StoredDietPreferences(maxDailyCarbsAmount = 100, dietMode = "KETO"),
            StoredDietPreferences(maxDailyCarbsAmount = 80, dietMode = "LOW_CARB"),
            StoredDietPreferences(maxDailyCarbsAmount = 40, dietMode = "LOW_CARB"),
            StoredDietPreferences(maxDailyCarbsAmount = 20, dietMode = "LOW_CARB"),
            StoredDietPreferences(maxDailyCarbsAmount = null, dietMode = "LOW_CARB"),
            StoredDietPreferences(maxDailyCarbsAmount = 25, dietMode = null),
        ).forEach { stored ->
            val persistence = FakePersistence(stored = listOf(stored))
            val repository = DietPreferencesRepositoryImpl(
                persistence = persistence,
                dispatchers = TestAppDispatchers(testScheduler),
            )

            assertEquals(
                Outcome.Success(DietPreset.KETO.preferences),
                repository.getDietPreferences(),
                "$stored",
            )
            assertTrue(persistence.saved.isEmpty(), "$stored")
        }
    }

    @Test
    fun `when two stored values that match no preset follow each other then keto is emitted once`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            persistence = FakePersistence(
                stored = listOf(
                    StoredDietPreferences(maxDailyCarbsAmount = 35, dietMode = "KETO"),
                    StoredDietPreferences(maxDailyCarbsAmount = 80, dietMode = "LOW_CARB"),
                ),
            ),
            dispatchers = TestAppDispatchers(testScheduler),
        )

        assertEquals(listOf(DietPreset.KETO.preferences), repository.observeDietPreferences().toList())
    }

    @Test
    fun `when the same preferences are stored twice then they are emitted once`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(amounts = listOf(null, 40, 20)),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(
            listOf(DietPreferences.Default, DietPreset.STRICT_KETO.preferences),
            repository.observeDietPreferences().toList(),
        )
    }

    @Test
    fun `when reading throws then a storage error is thrown and get returns a storage failure`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(failure = IllegalStateException("disk")),
            TestAppDispatchers(testScheduler),
        )

        val exception = assertFailsWith<AppErrorException> { repository.observeDietPreferences().toList() }
        assertEquals(AppError.Storage, exception.error)
        assertEquals(Outcome.Failure(AppError.Storage), repository.getDietPreferences())
    }

    @Test
    fun `when preferences are set then amount and mode name are saved in one call and success is returned`() =
        runTest {
            val persistence = FakePersistence()
            val repository = DietPreferencesRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

            assertEquals(
                Outcome.Success(Unit),
                repository.setDietPreferences(DietPreferences(100, DietMode.LOW_CARB)),
            )
            assertEquals(listOf(100 to "LOW_CARB"), persistence.saved)
        }

    @Test
    fun `when setting throws then storage failure is returned`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(failure = IllegalStateException("disk")),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(
            Outcome.Failure(AppError.Storage),
            repository.setDietPreferences(DietPreset.STRICT_KETO.preferences),
        )
    }

    @Test
    fun `when persistence is cancelled then cancellation is rethrown`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(failure = CancellationException("cancelled")),
            TestAppDispatchers(testScheduler),
        )

        assertFailsWith<CancellationException> { repository.observeDietPreferences().toList() }
        assertFailsWith<CancellationException> {
            repository.setDietPreferences(DietPreset.STRICT_KETO.preferences)
        }
    }

    private class FakePersistence(
        amounts: List<Int?> = emptyList(),
        private val stored: List<StoredDietPreferences> = amounts.map { StoredDietPreferences(it, null) },
        private val failure: Throwable? = null,
    ) : DietPreferencesPersistence {

        val saved = mutableListOf<Pair<Int, String>>()

        override fun observe(): Flow<StoredDietPreferences> {
            val failure = failure ?: return flowOf(*stored.toTypedArray())
            return flow { throw failure }
        }

        override suspend fun save(maxDailyCarbsAmount: Int, dietMode: String) {
            failure?.let { throw it }
            saved += maxDailyCarbsAmount to dietMode
        }

        override suspend fun completeSetup(maxDailyCarbsAmount: Int, dietMode: String) {
            failure?.let { throw it }
        }

        override suspend fun markTipShown(tip: OnboardingTip) {
            failure?.let { throw it }
        }
    }
}
