package pl.handsome.club.ketoscanner.ui

import pl.handsome.club.ketoscanner.navigation.BottomNavSection

internal sealed interface AppAction {
    data class OnDestinationChanged(val tab: BottomNavSection?) : AppAction
    data class OnTabClicked(val tab: BottomNavSection) : AppAction
}
