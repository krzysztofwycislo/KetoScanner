package app.carbpeek.feature.preferences.data

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.AppErrorException
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.testing.TestAppDispatchers
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.preferences.api.model.OnboardingTip
import app.carbpeek.feature.preferences.api.model.ProfileSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class ProfileSettingsRepositoryImplTest {

    @Test
    fun `when nothing is stored then the default settings are observed with the setting off`() = runTest {
        val repository = createRepository(
            FakePersistence(stored = listOf(StoredDietPreferences(maxDailyCarbsAmount = null, dietMode = null))),
        )

        assertEquals(listOf(ProfileSettings.Default), repository.observeSettings().toList())
        assertEquals(DietPreferences.Default, ProfileSettings.Default.dietPreferences)
        assertFalse(ProfileSettings.Default.opensDetailsRightAway)
    }

    @Test
    fun `when the setting is stored on or off then it is observed with every profile`() = runTest {
        val profiles = mapOf(
            (20 to "KETO") to DietPreset.STRICT_KETO.preferences,
            (40 to "KETO") to DietPreset.KETO.preferences,
            (100 to "LOW_CARB") to DietPreset.LOW_CARB.preferences,
            (40 to "NONE") to DietPreferences.NoDiet,
        )
        profiles.forEach { (stored, expected) ->
            listOf(true, false).forEach { isOn ->
                val repository = createRepository(
                    FakePersistence(
                        stored = listOf(
                            StoredDietPreferences(
                                maxDailyCarbsAmount = stored.first,
                                dietMode = stored.second,
                                opensDetailsRightAway = isOn,
                            ),
                        ),
                    ),
                )

                assertEquals(
                    listOf(ProfileSettings(dietPreferences = expected, opensDetailsRightAway = isOn)),
                    repository.observeSettings().toList(),
                )
            }
        }
    }

    @Test
    fun `when stored values differ only in tips or setup then the settings are emitted once`() = runTest {
        val stored = StoredDietPreferences(
            maxDailyCarbsAmount = 40,
            dietMode = "KETO",
            pendingTips = setOf(OnboardingTip.FIRST_SCAN_HINT),
            opensDetailsRightAway = true,
        )
        val repository = createRepository(
            FakePersistence(
                stored = listOf(stored, stored.copy(pendingTips = emptySet()), stored.copy(isSetupDone = true)),
            ),
        )

        assertEquals(
            listOf(ProfileSettings(dietPreferences = DietPreset.KETO.preferences, opensDetailsRightAway = true)),
            repository.observeSettings().toList(),
        )
    }

    @Test
    fun `when only the setting changes then the settings are emitted again`() = runTest {
        val stored = StoredDietPreferences(maxDailyCarbsAmount = 40, dietMode = "KETO")
        val repository = createRepository(
            FakePersistence(stored = listOf(stored, stored.copy(opensDetailsRightAway = true))),
        )

        assertEquals(
            listOf(
                ProfileSettings(dietPreferences = DietPreset.KETO.preferences, opensDetailsRightAway = false),
                ProfileSettings(dietPreferences = DietPreset.KETO.preferences, opensDetailsRightAway = true),
            ),
            repository.observeSettings().toList(),
        )
    }

    @Test
    fun `when settings are saved then amount mode and setting are saved in one call and success is returned`() =
        runTest {
            val persistence = FakePersistence()
            val repository = createRepository(persistence)

            val result = repository.saveSettings(
                ProfileSettings(dietPreferences = DietPreset.LOW_CARB.preferences, opensDetailsRightAway = true),
            )

            assertEquals(Outcome.Success(Unit), result)
            assertEquals(
                listOf(
                    StoredProfileSettings(
                        maxDailyCarbsAmount = 100,
                        dietMode = "LOW_CARB",
                        opensDetailsRightAway = true,
                    ),
                ),
                persistence.saved,
            )
        }

    @Test
    fun `when the setting is saved off then off is saved`() = runTest {
        val persistence = FakePersistence()
        val repository = createRepository(persistence)

        repository.saveSettings(
            ProfileSettings(dietPreferences = DietPreset.STRICT_KETO.preferences, opensDetailsRightAway = false),
        )

        assertEquals(
            listOf(StoredProfileSettings(maxDailyCarbsAmount = 20, dietMode = "KETO", opensDetailsRightAway = false)),
            persistence.saved,
        )
    }

    @Test
    fun `when no diet is saved then the default amount no mode and the setting are saved`() = runTest {
        val persistence = FakePersistence()
        val repository = createRepository(persistence)

        repository.saveSettings(
            ProfileSettings(dietPreferences = DietPreferences.NoDiet, opensDetailsRightAway = true),
        )

        assertEquals(
            listOf(StoredProfileSettings(maxDailyCarbsAmount = 40, dietMode = "NONE", opensDetailsRightAway = true)),
            persistence.saved,
        )
    }

    @Test
    fun `when reading or saving throws then a storage error is given`() = runTest {
        val repository = createRepository(FakePersistence(failure = IllegalStateException("disk")))

        val exception = assertFailsWith<AppErrorException> { repository.observeSettings().toList() }
        assertEquals(AppError.Storage, exception.error)
        assertEquals(Outcome.Failure(AppError.Storage), repository.saveSettings(ProfileSettings.Default))
    }

    @Test
    fun `when persistence is cancelled then cancellation is rethrown`() = runTest {
        val repository = createRepository(FakePersistence(failure = CancellationException("cancelled")))

        assertFailsWith<CancellationException> { repository.observeSettings().toList() }
        assertFailsWith<CancellationException> { repository.saveSettings(ProfileSettings.Default) }
    }

    private fun TestScope.createRepository(persistence: FakePersistence) = ProfileSettingsRepositoryImpl(
        persistence = persistence,
        dispatchers = TestAppDispatchers(testScheduler),
    )

    private class FakePersistence(
        private val stored: List<StoredDietPreferences> = emptyList(),
        private val failure: Throwable? = null,
    ) : DietPreferencesPersistence {

        val saved = mutableListOf<StoredProfileSettings>()

        override fun observe(): Flow<StoredDietPreferences> {
            val failure = failure ?: return flowOf(*stored.toTypedArray())
            return flow { throw failure }
        }

        override suspend fun save(settings: StoredProfileSettings) {
            failure?.let { throw it }
            saved += settings
        }

        override suspend fun completeSetup(maxDailyCarbsAmount: Int, dietMode: String) {
            throw NotImplementedError()
        }

        override suspend fun markTipShown(tip: OnboardingTip) {
            throw NotImplementedError()
        }
    }
}
