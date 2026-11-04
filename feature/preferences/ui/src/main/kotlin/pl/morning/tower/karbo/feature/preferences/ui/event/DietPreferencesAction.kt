package pl.morning.tower.karbo.feature.preferences.ui.event

import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.ui.state.DietProfileOption

internal sealed interface DietPreferencesAction {
    data class OnOptionSelected(val option: DietProfileOption) : DietPreferencesAction
    data class OnCustomModeSelected(val mode: DietMode) : DietPreferencesAction
    data class OnInputChanged(val text: String) : DietPreferencesAction
    data object OnSaveClicked : DietPreferencesAction
    data object OnRetryClicked : DietPreferencesAction
    data object OnBackClicked : DietPreferencesAction
}
