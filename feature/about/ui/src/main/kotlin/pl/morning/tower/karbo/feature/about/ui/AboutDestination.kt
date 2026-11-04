package pl.morning.tower.karbo.feature.about.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.morning.tower.karbo.feature.about.ui.event.AboutEvent.AboutNavigation

@Serializable
data object AboutRoute

fun NavGraphBuilder.aboutDestination(onNavigation: (AboutNavigation) -> Unit) {
    composable<AboutRoute> {
        AboutScreen(onNavigation)
    }
}
