package pl.handsome.club.feature.preferences.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.testing.TestAppDispatchers
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DietPreferencesRepositoryImplTest {

    @Test
    fun `when no amount is stored then default preferences are returned`() = runTest {
        val repository = DietPreferencesRepositoryImpl(FakePersistence(amounts = listOf(null)), TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Success(DietPreferences.Default), repository.getDietPreferences())
    }

    @Test
    fun `when an amount is stored then it is mapped to preferences`() = runTest {
        val repository = DietPreferencesRepositoryImpl(FakePersistence(amounts = listOf(25)), TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Success(DietPreferences(25)), repository.getDietPreferences())
    }

    @Test
    fun `when the same preferences are stored twice then they are emitted once`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(amounts = listOf(null, 40, 25)),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(
            listOf(Outcome.Success(DietPreferences.Default), Outcome.Success(DietPreferences(25))),
            repository.observeDietPreferences().toList(),
        )
    }

    @Test
    fun `when reading throws then storage failure is emitted`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(failure = IllegalStateException("disk")),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(listOf(Outcome.Failure(AppError.Storage)), repository.observeDietPreferences().toList())
        assertEquals(Outcome.Failure(AppError.Storage), repository.getDietPreferences())
    }

    @Test
    fun `when amount is set then it is passed to persistence and success is returned`() = runTest {
        val persistence = FakePersistence()
        val repository = DietPreferencesRepositoryImpl(persistence, TestAppDispatchers(testScheduler))

        assertEquals(Outcome.Success(Unit), repository.setMaxDailyCarbsAmount(55))
        assertEquals(listOf(55), persistence.setAmounts)
    }

    @Test
    fun `when setting throws then storage failure is returned`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(failure = IllegalStateException("disk")),
            TestAppDispatchers(testScheduler),
        )

        assertEquals(Outcome.Failure(AppError.Storage), repository.setMaxDailyCarbsAmount(55))
    }

    @Test
    fun `when persistence is cancelled then cancellation is rethrown`() = runTest {
        val repository = DietPreferencesRepositoryImpl(
            FakePersistence(failure = CancellationException("cancelled")),
            TestAppDispatchers(testScheduler),
        )

        assertFailsWith<CancellationException> { repository.observeDietPreferences().toList() }
        assertFailsWith<CancellationException> { repository.setMaxDailyCarbsAmount(55) }
    }

    private class FakePersistence(
        private val amounts: List<Int?> = emptyList(),
        private val failure: Throwable? = null,
    ) : DietPreferencesPersistence {

        val setAmounts = mutableListOf<Int>()

        override fun observeMaxDailyCarbsAmount(): Flow<Int?> {
            val failure = failure ?: return flowOf(*amounts.toTypedArray())
            return flow { throw failure }
        }

        override suspend fun setMaxDailyCarbsAmount(amount: Int) {
            failure?.let { throw it }
            setAmounts += amount
        }
    }
}
