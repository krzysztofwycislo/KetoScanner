package pl.morning.tower.karbo.feature.onboarding.ui.state

import androidx.compose.runtime.Immutable
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset

@Immutable
internal data class OnboardingStatePrototype(
    val selectedPreset: DietPreset = DietPreset.KETO,
    val isSaving: Boolean = false,
)
