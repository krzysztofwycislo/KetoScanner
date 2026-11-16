package app.carbpeek.navigation

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
import app.carbpeek.core.designsystem.theme.AppMotion
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.about.ui.AboutRoute
import app.carbpeek.feature.about.ui.aboutDestination
import app.carbpeek.feature.about.ui.event.AboutEvent.AboutNavigation
import app.carbpeek.feature.favourites.ui.FavouritesRoute
import app.carbpeek.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation
import app.carbpeek.feature.favourites.ui.favouritesDestination
import app.carbpeek.feature.history.ui.HistoryRoute
import app.carbpeek.feature.history.ui.event.HistoryEvent.HistoryNavigation
import app.carbpeek.feature.history.ui.historyDestination
import app.carbpeek.feature.home.ui.HomeRoute
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation
import app.carbpeek.feature.home.ui.homeDestination
import app.carbpeek.feature.onboarding.ui.OnboardingRoute
import app.carbpeek.feature.onboarding.ui.event.OnboardingEvent.OnboardingNavigation
import app.carbpeek.feature.onboarding.ui.onboardingDestination
import app.carbpeek.feature.preferences.ui.DietPreferencesRoute
import app.carbpeek.feature.preferences.ui.dietPreferencesDestination
import app.carbpeek.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation
import app.carbpeek.feature.product.ui.ProductRoute
import app.carbpeek.feature.product.ui.event.ProductEvent.ProductNavigation
import app.carbpeek.feature.product.ui.productDestination
import app.carbpeek.feature.report.ui.ReportKind
import app.carbpeek.feature.report.ui.ReportRoute
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation
import app.carbpeek.feature.report.ui.reportDestination
import app.carbpeek.feature.scanner.ui.ScanRoute
import app.carbpeek.feature.scanner.ui.event.ScanEvent.ScanNavigation
import app.carbpeek.feature.scanner.ui.scanDestination

@Composable
internal fun AppNavHost(navController: NavHostController, start: AppStart, modifier: Modifier = Modifier) {
    val motion = AppTheme.motion
    NavHost(
        navController = navController,
        startDestination = when (start) {
            AppStart.HOME -> HomeRoute
            AppStart.ONBOARDING -> OnboardingRoute
        },
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
        onboardingDestination { navigation ->
            navController.ifCurrent<OnboardingRoute> {
                when (navigation) {
                    OnboardingNavigation.NavigateToScan -> {
                        navigate(HomeRoute) {
                            popUpTo<OnboardingRoute> { inclusive = true }
                        }
                        navigateToTab(BottomNavSection.SCAN)
                    }
                }
            }
        }
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
                    HistoryNavigation.NavigateToScan -> navigateToTab(BottomNavSection.SCAN)
                }
            }
        }
        scanDestination { navigation ->
            navController.ifCurrent<ScanRoute> {
                when (navigation) {
                    is ScanNavigation.NavigateToProduct -> navigate(ProductRoute(navigation.barcode))
                    is ScanNavigation.NavigateToAddProduct -> {
                        navigate(ReportRoute(barcode = navigation.barcode, kind = ReportKind.MISSING_PRODUCT))
                    }
                }
            }
        }
        favouritesDestination { navigation ->
            navController.ifCurrent<FavouritesRoute> {
                when (navigation) {
                    is FavouritesNavigation.NavigateToProduct -> navigate(ProductRoute(navigation.barcode))
                    FavouritesNavigation.NavigateToScan -> navigateToTab(BottomNavSection.SCAN)
                }
            }
        }
        productDestination { navigation ->
            navController.ifCurrent<ProductRoute> {
                when (navigation) {
                    ProductNavigation.NavigateBack -> navigateUp()
                    is ProductNavigation.NavigateToReport -> {
                        navigate(ReportRoute(barcode = navigation.barcode, kind = ReportKind.ISSUE))
                    }
                    is ProductNavigation.NavigateToAddProduct -> {
                        navigate(ReportRoute(barcode = navigation.barcode, kind = ReportKind.MISSING_PRODUCT))
                    }
                }
            }
        }
        reportDestination { navigation ->
            navController.ifCurrent<ReportRoute> {
                when (navigation) {
                    ReportNavigation.NavigateBack -> navigateUp()
                    ReportNavigation.NavigateToHome -> popBackStack<HomeRoute>(inclusive = false)
                    ReportNavigation.NavigateToScan -> {
                        popBackStack<HomeRoute>(inclusive = false)
                        navigateToTab(BottomNavSection.SCAN)
                    }
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

private fun NavBackStackEntry.isPushed(): Boolean = destination.presentation() == RoutePresentation.PUSHED

private inline fun <reified T : Any> NavHostController.ifCurrent(action: NavHostController.() -> Unit) {
    if (currentDestination?.hasRoute<T>() == true) action()
}
