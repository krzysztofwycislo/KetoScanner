package pl.morning.tower.karbo.feature.preferences.api

import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset

interface OnboardingRepository {

    suspend fun isSetupDone(): Outcome<Boolean>

    suspend fun completeSetup(preset: DietPreset): Outcome<Unit>
}
