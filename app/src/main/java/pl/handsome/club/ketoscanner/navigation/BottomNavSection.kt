package pl.handsome.club.ketoscanner.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import pl.handsome.club.core.designsystem.icon.AppIcon
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.feature.favourites.ui.FavouritesRoute
import pl.handsome.club.feature.home.ui.HomeRoute
import pl.handsome.club.feature.scanner.ui.ScanRoute
import pl.handsome.club.ketoscanner.R

internal enum class BottomNavSection(
    val route: Any,
    @StringRes val label: Int,
    val icon: AppIcon,
    val selectedIcon: AppIcon = icon,
    val isProminent: Boolean = false,
) {
    HOME(HomeRoute, R.string.bottom_navigation_home, AppIcons.Home),
    SCAN(ScanRoute, R.string.bottom_navigation_scan, AppIcons.Scan, isProminent = true),
    FAVOURITES(FavouritesRoute, R.string.bottom_navigation_favourites, AppIcons.Heart, AppIcons.HeartFilled),
}

internal fun NavDestination.toBottomNavSection(): BottomNavSection? =
    BottomNavSection.entries.firstOrNull { hasRoute(it.route::class) }

internal fun NavHostController.navigateToTab(tab: BottomNavSection) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
