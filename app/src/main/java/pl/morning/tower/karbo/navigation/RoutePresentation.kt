package pl.morning.tower.karbo.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import pl.morning.tower.karbo.feature.about.ui.AboutRoute
import pl.morning.tower.karbo.feature.history.ui.HistoryRoute
import pl.morning.tower.karbo.feature.onboarding.ui.OnboardingRoute
import pl.morning.tower.karbo.feature.preferences.ui.DietPreferencesRoute
import pl.morning.tower.karbo.feature.product.ui.ProductRoute
import kotlin.reflect.KClass

internal enum class RoutePresentation {
    STANDARD,
    PUSHED,
    FULL_SCREEN,
}

private val routePresentations: Map<KClass<*>, RoutePresentation> = mapOf(
    ProductRoute::class to RoutePresentation.PUSHED,
    DietPreferencesRoute::class to RoutePresentation.PUSHED,
    AboutRoute::class to RoutePresentation.PUSHED,
    HistoryRoute::class to RoutePresentation.PUSHED,
    OnboardingRoute::class to RoutePresentation.FULL_SCREEN,
)

internal fun NavDestination.presentation(): RoutePresentation =
    routePresentations.entries
        .firstOrNull { (route, _) -> hasRoute(route) }
        ?.value
        ?: RoutePresentation.STANDARD
