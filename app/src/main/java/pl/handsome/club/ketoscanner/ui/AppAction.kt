package pl.handsome.club.ketoscanner.ui

import pl.handsome.club.ketoscanner.navigation.TopLevelTab

internal sealed interface AppAction {
    data class OnDestinationChanged(val tab: TopLevelTab?) : AppAction
    data class OnTabClicked(val tab: TopLevelTab) : AppAction
}
