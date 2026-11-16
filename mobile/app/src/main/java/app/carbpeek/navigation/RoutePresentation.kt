package app.carbpeek.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import app.carbpeek.feature.about.ui.AboutRoute
import app.carbpeek.feature.history.ui.HistoryRoute
import app.carbpeek.feature.onboarding.ui.OnboardingRoute
import app.carbpeek.feature.preferences.ui.DietPreferencesRoute
import app.carbpeek.feature.product.ui.ProductRoute
import app.carbpeek.feature.report.ui.ReportRoute
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
    ReportRoute::class to RoutePresentation.FULL_SCREEN,
)

internal fun NavDestination.presentation(): RoutePresentation =
    routePresentations.entries
        .firstOrNull { (route, _) -> hasRoute(route) }
        ?.value
        ?: RoutePresentation.STANDARD
