package app.carbpeek.feature.preferences.ui.event

import app.carbpeek.feature.preferences.ui.state.DietProfileOption

internal sealed interface DietPreferencesAction {
    data class OnOptionSelected(val option: DietProfileOption) : DietPreferencesAction
    data class OnDetailsSwitchChanged(val isOn: Boolean) : DietPreferencesAction
    data object OnSaveClicked : DietPreferencesAction
    data object OnRetryClicked : DietPreferencesAction
    data object OnBackClicked : DietPreferencesAction
}
