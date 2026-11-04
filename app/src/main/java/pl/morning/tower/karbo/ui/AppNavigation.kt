package pl.handsome.club.ketoscanner.ui

import pl.handsome.club.ketoscanner.navigation.BottomNavSection

internal sealed interface AppNavigation {
    data class NavigateToTab(val tab: BottomNavSection) : AppNavigation
    data class PopToTabRoot(val tab: BottomNavSection) : AppNavigation
}
