package pl.handsome.club.feature.history.ui.event

sealed interface HistoryEvent {

    sealed interface HistoryNavigation : HistoryEvent {
        data class NavigateToProduct(val barcode: String) : HistoryNavigation
        data object NavigateToDietPreferences : HistoryNavigation
        data object NavigateToAbout : HistoryNavigation
    }
}
