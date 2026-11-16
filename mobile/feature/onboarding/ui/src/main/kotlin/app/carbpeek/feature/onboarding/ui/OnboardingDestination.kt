package app.carbpeek.feature.onboarding.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.carbpeek.feature.onboarding.ui.event.OnboardingEvent.OnboardingNavigation
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

fun NavGraphBuilder.onboardingDestination(onNavigation: (OnboardingNavigation) -> Unit) {
    composable<OnboardingRoute> {
        OnboardingScreen(onNavigation)
    }
}
