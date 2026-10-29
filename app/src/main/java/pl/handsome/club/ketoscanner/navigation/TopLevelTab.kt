package pl.handsome.club.ketoscanner.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.feature.favourites.ui.FavouritesRoute
import pl.handsome.club.feature.history.ui.HistoryRoute
import pl.handsome.club.feature.scanner.ui.ScanRoute
import pl.handsome.club.ketoscanner.R

internal enum class TopLevelTab(
    val route: Any,
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val isProminent: Boolean = false,
) {
    HOME(HistoryRoute, R.string.bottom_navigation_home, AppIcons.Home),
    SCAN(ScanRoute, R.string.bottom_navigation_scan, AppIcons.Scan, isProminent = true),
    FAVOURITES(FavouritesRoute, R.string.bottom_navigation_favourites, AppIcons.Heart, AppIcons.HeartFilled),
}

internal fun NavDestination.topLevelTab(): TopLevelTab? =
    TopLevelTab.entries.firstOrNull { hasRoute(it.route::class) }
