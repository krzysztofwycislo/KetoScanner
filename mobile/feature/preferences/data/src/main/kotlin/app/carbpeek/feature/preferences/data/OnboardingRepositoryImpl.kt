package app.carbpeek.feature.preferences.data

import app.carbpeek.core.common.AppDispatchers
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.outcomeOf
import app.carbpeek.feature.preferences.api.OnboardingRepository
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.preferences.api.model.OnboardingTip
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

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
