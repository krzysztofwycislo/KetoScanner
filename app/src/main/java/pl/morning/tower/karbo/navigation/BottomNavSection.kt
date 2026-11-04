package pl.morning.tower.karbo.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.feature.favourites.ui.FavouritesRoute
import pl.morning.tower.karbo.feature.home.ui.HomeRoute
import pl.morning.tower.karbo.feature.scanner.ui.ScanRoute
import pl.morning.tower.karbo.R

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
