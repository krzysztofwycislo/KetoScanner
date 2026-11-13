package pl.morning.tower.karbo.feature.preferences.api

import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset
import pl.morning.tower.karbo.feature.preferences.api.model.OnboardingTip

interface OnboardingRepository {

    suspend fun isSetupDone(): Outcome<Boolean>

    suspend fun completeSetup(preset: DietPreset): Outcome<Unit>

    suspend fun pendingTips(): Outcome<Set<OnboardingTip>>

    suspend fun markTipShown(tip: OnboardingTip): Outcome<Unit>
}
