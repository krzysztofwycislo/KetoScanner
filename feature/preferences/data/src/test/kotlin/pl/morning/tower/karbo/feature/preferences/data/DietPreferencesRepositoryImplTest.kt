package pl.morning.tower.karbo.feature.preferences.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.AppErrorException
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.testing.TestAppDispatchers
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.preferences.api.model.OnboardingTip
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
            FakePersistence(amounts = listOf(25)),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(Outcome.Success(DietPreferences(25)), repository.getDietPreferences())
    }

    @Test
    fun `when no mode is stored then mode is keto`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(stored = listOf(StoredDietPreferences(25, null))),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(Outcome.Success(DietPreferences(25, DietMode.KETO)), repository.getDietPreferences())
    }

    @Test
    fun `when an unknown mode is stored then mode is keto`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(stored = listOf(StoredDietPreferences(25, "PALEO"))),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(Outcome.Success(DietPreferences(25, DietMode.KETO)), repository.getDietPreferences())
    }

    @Test
    fun `when low carb mode is stored then it is mapped to preferences`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(stored = listOf(StoredDietPreferences(null, "LOW_CARB"))),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(
            Outcome.Success(DietPreferences(DietPreferences.Default.maxDailyCarbsAmount, DietMode.LOW_CARB)),
            repository.getDietPreferences(),
        )
    }

    @Test
    fun `when the same preferences are stored twice then they are emitted once`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(amounts = listOf(null, 40, 25)),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(
            listOf(DietPreferences.Default, DietPreferences(25)),
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

        assertEquals(Outcome.Failure(AppError.Storage), repository.setDietPreferences(DietPreferences(55)))
    }

    @Test
    fun `when persistence is cancelled then cancellation is rethrown`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(failure = CancellationException("cancelled")),
            TestAppDispatchers(testScheduler),
        )

        assertFailsWith<CancellationException> { repository.observeDietPreferences().toList() }
        assertFailsWith<CancellationException> { repository.setDietPreferences(DietPreferences(55)) }
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
