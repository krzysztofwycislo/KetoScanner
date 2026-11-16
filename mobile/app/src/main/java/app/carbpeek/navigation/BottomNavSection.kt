package app.carbpeek.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import app.carbpeek.R
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.feature.favourites.ui.FavouritesRoute
import app.carbpeek.feature.home.ui.HomeRoute
import app.carbpeek.feature.scanner.ui.ScanRoute

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
        popUpTo(HomeRoute) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

internal fun NavHostController.startScanning() {
    clearBackStack<ScanRoute>()
    navigate(ScanRoute) {
        popUpTo(HomeRoute) { saveState = true }
        launchSingleTop = true
    }
}
