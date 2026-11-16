package app.carbpeek.feature.home.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavGraphBuilder.homeDestination(onNavigation: (HomeNavigation) -> Unit) {
    composable<HomeRoute> {
        HomeScreen(onNavigation)
    }
}
