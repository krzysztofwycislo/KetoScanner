package pl.handsome.club.feature.preferences.ui.event

internal sealed interface DietPreferencesAction {
    data class OnInputChanged(val text: String) : DietPreferencesAction
    data object OnSaveClicked : DietPreferencesAction
    data object OnRetryClicked : DietPreferencesAction
    data object OnBackClicked : DietPreferencesAction
}
