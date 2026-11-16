package app.carbpeek.ui

import app.carbpeek.navigation.BottomNavSection
import app.carbpeek.navigation.RoutePresentation

internal sealed interface AppAction {
    data class OnDestinationChanged(val tab: BottomNavSection?, val presentation: RoutePresentation) : AppAction
    data class OnTabClicked(val tab: BottomNavSection) : AppAction
}
