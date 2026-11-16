package app.carbpeek.feature.preferences.api

import app.carbpeek.core.common.Outcome
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.preferences.api.model.OnboardingTip

interface OnboardingRepository {

    suspend fun isSetupDone(): Outcome<Boolean>

    suspend fun completeSetup(preset: DietPreset): Outcome<Unit>

    suspend fun pendingTips(): Outcome<Set<OnboardingTip>>

    suspend fun markTipShown(tip: OnboardingTip): Outcome<Unit>
}
