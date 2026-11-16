package app.carbpeek.feature.about.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.carbpeek.feature.about.ui.event.AboutEvent.AboutNavigation
import kotlinx.serialization.Serializable

@Serializable
data object AboutRoute

fun NavGraphBuilder.aboutDestination(onNavigation: (AboutNavigation) -> Unit) {
    composable<AboutRoute> {
        AboutScreen(onNavigation)
    }
}
