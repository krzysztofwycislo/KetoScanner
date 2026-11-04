package pl.morning.tower.karbo.ui

import pl.morning.tower.karbo.navigation.BottomNavSection

internal sealed interface AppAction {
    data class OnDestinationChanged(val tab: BottomNavSection?) : AppAction
    data class OnTabClicked(val tab: BottomNavSection) : AppAction
}
