package pl.handsome.club.feature.home.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.handsome.club.feature.home.ui.event.HomeEvent.HomeNavigation

@Serializable
data object HomeRoute

fun NavGraphBuilder.homeDestination(onNavigation: (HomeNavigation) -> Unit) {
    composable<HomeRoute> {
        HomeScreen(onNavigation)
    }
}
