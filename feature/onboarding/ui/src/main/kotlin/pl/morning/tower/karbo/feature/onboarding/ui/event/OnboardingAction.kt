package pl.morning.tower.karbo.feature.onboarding.ui.event

import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset

internal sealed interface OnboardingAction {
    data class OnPresetSelected(val preset: DietPreset) : OnboardingAction
    data object OnStartScanningClicked : OnboardingAction
    data object OnSkipClicked : OnboardingAction
}
