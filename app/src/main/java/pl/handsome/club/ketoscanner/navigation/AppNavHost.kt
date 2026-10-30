package pl.handsome.club.ketoscanner.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import pl.handsome.club.feature.about.ui.AboutRoute
import pl.handsome.club.feature.about.ui.aboutDestination
import pl.handsome.club.feature.about.ui.event.AboutEvent.AboutNavigation
import pl.handsome.club.feature.favourites.ui.FavouritesRoute
import pl.handsome.club.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation
import pl.handsome.club.feature.favourites.ui.favouritesDestination
import pl.handsome.club.feature.history.ui.HistoryRoute
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation
import pl.handsome.club.feature.history.ui.historyDestination
import pl.handsome.club.feature.home.ui.HomeRoute
import pl.handsome.club.feature.home.ui.event.HomeEvent.HomeNavigation
import pl.handsome.club.feature.home.ui.homeDestination
import pl.handsome.club.feature.preferences.ui.DietPreferencesRoute
import pl.handsome.club.feature.preferences.ui.dietPreferencesDestination
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation
import pl.handsome.club.feature.product.ui.ProductRoute
import pl.handsome.club.feature.product.ui.event.ProductEvent.ProductNavigation
import pl.handsome.club.feature.product.ui.productDestination
import pl.handsome.club.feature.scanner.ui.ScanRoute
import pl.handsome.club.feature.scanner.ui.event.ScanEvent.ScanNavigation
import pl.handsome.club.feature.scanner.ui.scanDestination
import kotlin.reflect.KClass

private const val TRANSITION_DURATION_MILLIS = 200

private val pushedRoutes: List<KClass<*>> =
    listOf(ProductRoute::class, DietPreferencesRoute::class, AboutRoute::class, HistoryRoute::class)

@Composable
internal fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = {
            if (targetState.isPushed()) {
                slideIntoContainer(SlideDirection.Start, transitionSpec())
            } else {
                fadeIn(transitionSpec())
            }
        },
        exitTransition = {
            if (targetState.isPushed()) {
                slideOutOfContainer(SlideDirection.Start, transitionSpec())
            } else {
                fadeOut(transitionSpec())
            }
        },
        popEnterTransition = {
            if (initialState.isPushed()) {
                slideIntoContainer(SlideDirection.End, transitionSpec())
            } else {
                fadeIn(transitionSpec())
            }
        },
        popExitTransition = {
            if (initialState.isPushed()) {
                slideOutOfContainer(SlideDirection.End, transitionSpec())
            } else {
                fadeOut(transitionSpec())
            }
        },
    ) {
        homeDestination { navigation ->
            navController.ifCurrent<HomeRoute> {
                when (navigation) {
                    is HomeNavigation.NavigateToProduct -> navigate(ProductRoute(navigation.barcode))
                    HomeNavigation.NavigateToDietPreferences -> navigate(DietPreferencesRoute)
                    HomeNavigation.NavigateToAbout -> navigate(AboutRoute)
                    HomeNavigation.NavigateToHistory -> navigate(HistoryRoute)
                    HomeNavigation.NavigateToScan -> navigateToTab(TopLevelTab.SCAN)
                }
            }
        }
        historyDestination { navigation ->
            navController.ifCurrent<HistoryRoute> {
                when (navigation) {
                    is HistoryNavigation.NavigateToProduct -> navigate(ProductRoute(navigation.barcode))
                    HistoryNavigation.NavigateBack -> navigateUp()
                }
            }
        }
        scanDestination { navigation ->
            navController.ifCurrent<ScanRoute> {
                when (navigation) {
                    is ScanNavigation.NavigateToProduct -> navigate(ProductRoute(navigation.barcode))
                }
            }
        }
        favouritesDestination { navigation ->
            navController.ifCurrent<FavouritesRoute> {
                when (navigation) {
                    is FavouritesNavigation.NavigateToProduct -> navigate(ProductRoute(navigation.barcode))
                }
            }
        }
        productDestination { navigation ->
            navController.ifCurrent<ProductRoute> {
                when (navigation) {
                    ProductNavigation.NavigateBack -> navigateUp()
                }
            }
        }
        dietPreferencesDestination { navigation ->
            navController.ifCurrent<DietPreferencesRoute> {
                when (navigation) {
                    DietPreferencesNavigation.NavigateBack -> navigateUp()
                }
            }
        }
        aboutDestination { navigation ->
            navController.ifCurrent<AboutRoute> {
                when (navigation) {
                    AboutNavigation.NavigateBack -> navigateUp()
                }
            }
        }
    }
}

private fun <T> transitionSpec() = tween<T>(TRANSITION_DURATION_MILLIS, easing = LinearOutSlowInEasing)

private fun NavBackStackEntry.isPushed(): Boolean = pushedRoutes.any { destination.hasRoute(it) }

private inline fun <reified T : Any> NavHostController.ifCurrent(action: NavHostController.() -> Unit) {
    if (currentDestination?.hasRoute<T>() == true) action()
}
