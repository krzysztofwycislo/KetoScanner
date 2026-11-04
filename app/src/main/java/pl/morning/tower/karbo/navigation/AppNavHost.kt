package pl.morning.tower.karbo.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import pl.morning.tower.karbo.core.designsystem.theme.AppMotion
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.about.ui.AboutRoute
import pl.morning.tower.karbo.feature.about.ui.aboutDestination
import pl.morning.tower.karbo.feature.about.ui.event.AboutEvent.AboutNavigation
import pl.morning.tower.karbo.feature.favourites.ui.FavouritesRoute
import pl.morning.tower.karbo.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation
import pl.morning.tower.karbo.feature.favourites.ui.favouritesDestination
import pl.morning.tower.karbo.feature.history.ui.HistoryRoute
import pl.morning.tower.karbo.feature.history.ui.event.HistoryEvent.HistoryNavigation
import pl.morning.tower.karbo.feature.history.ui.historyDestination
import pl.morning.tower.karbo.feature.home.ui.HomeRoute
import pl.morning.tower.karbo.feature.home.ui.event.HomeEvent.HomeNavigation
import pl.morning.tower.karbo.feature.home.ui.homeDestination
import pl.morning.tower.karbo.feature.preferences.ui.DietPreferencesRoute
import pl.morning.tower.karbo.feature.preferences.ui.dietPreferencesDestination
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation
import pl.morning.tower.karbo.feature.product.ui.ProductRoute
import pl.morning.tower.karbo.feature.product.ui.event.ProductEvent.ProductNavigation
import pl.morning.tower.karbo.feature.product.ui.productDestination
import pl.morning.tower.karbo.feature.scanner.ui.ScanRoute
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanEvent.ScanNavigation
import pl.morning.tower.karbo.feature.scanner.ui.scanDestination
import kotlin.reflect.KClass

private val pushedRoutes: List<KClass<*>> =
    listOf(ProductRoute::class, DietPreferencesRoute::class, AboutRoute::class, HistoryRoute::class)

@Composable
internal fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    val motion = AppTheme.motion
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = {
            if (targetState.isPushed()) {
                slideIntoContainer(SlideDirection.Start, motion.transitionSpec())
            } else {
                fadeIn(motion.transitionSpec())
            }
        },
        exitTransition = {
            if (targetState.isPushed()) {
                slideOutOfContainer(SlideDirection.Start, motion.transitionSpec())
            } else {
                fadeOut(motion.transitionSpec())
            }
        },
        popEnterTransition = {
            if (initialState.isPushed()) {
                slideIntoContainer(SlideDirection.End, motion.transitionSpec())
            } else {
                fadeIn(motion.transitionSpec())
            }
        },
        popExitTransition = {
            if (initialState.isPushed()) {
                slideOutOfContainer(SlideDirection.End, motion.transitionSpec())
            } else {
                fadeOut(motion.transitionSpec())
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
                    HomeNavigation.NavigateToScan -> navigateToTab(BottomNavSection.SCAN)
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

private fun <T> AppMotion.transitionSpec() = tween<T>(durationMedium, easing = easingStandard)

private fun NavBackStackEntry.isPushed(): Boolean = pushedRoutes.any { destination.hasRoute(it) }

private inline fun <reified T : Any> NavHostController.ifCurrent(action: NavHostController.() -> Unit) {
    if (currentDestination?.hasRoute<T>() == true) action()
}
