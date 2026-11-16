package app.carbpeek.feature.onboarding.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.feature.preferences.api.model.DietPreset

@Immutable
internal data class OnboardingStatePrototype(
    val selectedPreset: DietPreset = DietPreset.KETO,
    val isSaving: Boolean = false,
)
