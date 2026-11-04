package pl.morning.tower.karbo.feature.home.ui.event

internal sealed interface HomeAction {
    data object OnStart : HomeAction
    data object OnRetryClicked : HomeAction
    data object OnProfileClicked : HomeAction
    data object OnAboutClicked : HomeAction
    data object OnStartScanningClicked : HomeAction
    data object OnSeeAllClicked : HomeAction
    data class OnEntryClicked(val barcode: String) : HomeAction
}
