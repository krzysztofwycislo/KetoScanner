package app.carbpeek.feature.preferences.ui.event

sealed interface DietPreferencesEvent {

    sealed interface DietPreferencesNavigation : DietPreferencesEvent {
        data object NavigateBack : DietPreferencesNavigation
    }
}
