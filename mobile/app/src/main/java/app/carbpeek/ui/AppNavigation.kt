package app.carbpeek.ui

import app.carbpeek.navigation.BottomNavSection

internal sealed interface AppNavigation {
    data class NavigateToTab(val tab: BottomNavSection) : AppNavigation
    data class PopToTabRoot(val tab: BottomNavSection) : AppNavigation
}
