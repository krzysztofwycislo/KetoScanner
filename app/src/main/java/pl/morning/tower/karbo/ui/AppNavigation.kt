package pl.morning.tower.karbo.ui

import pl.morning.tower.karbo.navigation.BottomNavSection

internal sealed interface AppNavigation {
    data class NavigateToTab(val tab: BottomNavSection) : AppNavigation
    data class PopToTabRoot(val tab: BottomNavSection) : AppNavigation
}
