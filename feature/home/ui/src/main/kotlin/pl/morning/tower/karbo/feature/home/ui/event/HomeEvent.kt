package pl.morning.tower.karbo.feature.home.ui.event

sealed interface HomeEvent {

    sealed interface HomeNavigation : HomeEvent {
        data class NavigateToProduct(val barcode: String) : HomeNavigation
        data object NavigateToDietPreferences : HomeNavigation
        data object NavigateToAbout : HomeNavigation
        data object NavigateToScan : HomeNavigation
        data object NavigateToHistory : HomeNavigation
    }
}
