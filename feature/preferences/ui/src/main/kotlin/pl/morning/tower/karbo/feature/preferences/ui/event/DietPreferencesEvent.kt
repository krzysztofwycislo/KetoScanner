package pl.handsome.club.feature.preferences.ui.event

sealed interface DietPreferencesEvent {

    sealed interface DietPreferencesNavigation : DietPreferencesEvent {
        data object NavigateBack : DietPreferencesNavigation
    }
}
