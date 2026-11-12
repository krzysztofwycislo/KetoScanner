package pl.morning.tower.karbo.feature.history.ui.event

sealed interface HistoryEvent {

    sealed interface HistoryNavigation : HistoryEvent {
        data class NavigateToProduct(val barcode: String) : HistoryNavigation
        data object NavigateBack : HistoryNavigation
        data object NavigateToScan : HistoryNavigation
    }
}
