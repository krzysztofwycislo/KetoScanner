package pl.morning.tower.karbo.ui

import pl.morning.tower.karbo.navigation.BottomNavSection
import pl.morning.tower.karbo.navigation.RoutePresentation

internal sealed interface AppAction {
    data class OnDestinationChanged(val tab: BottomNavSection?, val presentation: RoutePresentation) : AppAction
    data class OnTabClicked(val tab: BottomNavSection) : AppAction
}
