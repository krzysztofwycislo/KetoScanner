package pl.handsome.club.ketoscanner.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import pl.handsome.club.feature.favourites.ui.FavouritesRoute
import pl.handsome.club.feature.history.ui.HistoryRoute
import pl.handsome.club.feature.scanner.ui.ScanRoute
import pl.handsome.club.ketoscanner.R

internal enum class TopLevelTab(val route: Any, @StringRes val label: Int, @DrawableRes val icon: Int) {
    HOME(HistoryRoute, R.string.bottom_navigation_home, R.drawable.home_black_36dp),
    SCAN(ScanRoute, R.string.bottom_navigation_scan, R.drawable.ic_barcode_black),
    FAVOURITES(FavouritesRoute, R.string.bottom_navigation_favourites, R.drawable.favorite_white_36dp),
}

internal fun NavDestination.topLevelTab(): TopLevelTab? =
    TopLevelTab.entries.firstOrNull { hasRoute(it.route::class) }
