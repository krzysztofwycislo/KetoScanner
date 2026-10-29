package pl.handsome.club.feature.preferences.ui.event

import pl.handsome.club.feature.preferences.api.model.DietMode

internal sealed interface DietPreferencesAction {
    data class OnDietModeSelected(val mode: DietMode) : DietPreferencesAction
    data class OnInputChanged(val text: String) : DietPreferencesAction
    data object OnSaveClicked : DietPreferencesAction
    data object OnRetryClicked : DietPreferencesAction
    data object OnBackClicked : DietPreferencesAction
}
