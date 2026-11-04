package pl.morning.tower.karbo.feature.home.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.morning.tower.karbo.feature.home.ui.event.HomeEvent.HomeNavigation

@Serializable
data object HomeRoute

fun NavGraphBuilder.homeDestination(onNavigation: (HomeNavigation) -> Unit) {
    composable<HomeRoute> {
        HomeScreen(onNavigation)
    }
}
