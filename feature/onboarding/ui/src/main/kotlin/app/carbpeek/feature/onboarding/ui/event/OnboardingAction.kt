package app.carbpeek.feature.onboarding.ui.event

import app.carbpeek.feature.preferences.api.model.DietPreset

internal sealed interface OnboardingAction {
    data class OnPresetSelected(val preset: DietPreset) : OnboardingAction
    data object OnStartScanningClicked : OnboardingAction
    data object OnSkipClicked : OnboardingAction
}
