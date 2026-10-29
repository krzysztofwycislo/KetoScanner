package pl.handsome.club.feature.history.ui.event

internal sealed interface HistoryAction {
    data object OnStart : HistoryAction
    data object OnRetryClicked : HistoryAction
    data class OnEntryClicked(val barcode: String) : HistoryAction
    data object OnMenuClicked : HistoryAction
    data object OnMenuDismissed : HistoryAction
    data object OnDietPreferencesClicked : HistoryAction
    data object OnAboutClicked : HistoryAction
}
