package pl.morning.tower.karbo.feature.preferences.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import pl.morning.tower.karbo.core.common.AppDispatchers
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.outcomeOf
import pl.morning.tower.karbo.feature.preferences.api.OnboardingRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset
import pl.morning.tower.karbo.feature.preferences.api.model.OnboardingTip

internal class OnboardingRepositoryImpl(
    private val persistence: DietPreferencesPersistence,
    private val dispatchers: AppDispatchers,
) : OnboardingRepository {

    override suspend fun isSetupDone(): Outcome<Boolean> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                val stored = persistence.observe().first()
                stored.isSetupDone || stored.maxDailyCarbsAmount != null || stored.dietMode != null
            }
        }

    override suspend fun completeSetup(preset: DietPreset): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.completeSetup(
                    maxDailyCarbsAmount = preset.preferences.maxDailyCarbsAmount,
                    dietMode = preset.preferences.dietMode.name,
                )
            }
        }

    override suspend fun pendingTips(): Outcome<Set<OnboardingTip>> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.observe().first().pendingTips
            }
        }

    override suspend fun markTipShown(tip: OnboardingTip): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                persistence.markTipShown(tip)
            }
        }
}
