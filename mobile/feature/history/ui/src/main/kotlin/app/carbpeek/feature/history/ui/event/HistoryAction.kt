package app.carbpeek.feature.history.ui.event

import app.carbpeek.feature.history.ui.state.HistoryFilter

internal sealed interface HistoryAction {
    data object OnStart : HistoryAction
    data object OnStartScanningClicked : HistoryAction
    data object OnRetryClicked : HistoryAction
    data class OnEntryClicked(val barcode: String) : HistoryAction
    data object OnBackClicked : HistoryAction
    data class OnQueryChanged(val query: String) : HistoryAction
    data object OnSearchSubmitted : HistoryAction
    data class OnFilterClicked(val filter: HistoryFilter) : HistoryAction
}
