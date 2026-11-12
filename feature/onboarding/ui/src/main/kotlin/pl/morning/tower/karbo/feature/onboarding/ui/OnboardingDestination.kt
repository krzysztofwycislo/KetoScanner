package pl.morning.tower.karbo.feature.onboarding.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.morning.tower.karbo.feature.onboarding.ui.event.OnboardingEvent.OnboardingNavigation

@Serializable
data object OnboardingRoute

fun NavGraphBuilder.onboardingDestination(onNavigation: (OnboardingNavigation) -> Unit) {
    composable<OnboardingRoute> {
        OnboardingScreen(onNavigation)
    }
}
