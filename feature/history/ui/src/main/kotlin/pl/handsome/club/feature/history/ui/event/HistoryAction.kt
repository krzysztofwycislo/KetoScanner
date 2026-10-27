package pl.handsome.club.feature.history.ui.event

internal sealed interface HistoryAction {
    data object OnStart : HistoryAction
    data object OnRetryClicked : HistoryAction
    data class OnEntryClicked(val barcode: String) : HistoryAction
}
