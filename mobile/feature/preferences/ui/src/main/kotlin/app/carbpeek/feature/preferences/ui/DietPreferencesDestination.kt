package app.carbpeek.feature.preferences.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.carbpeek.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation
import kotlinx.serialization.Serializable

@Serializable
data object DietPreferencesRoute

fun NavGraphBuilder.dietPreferencesDestination(onNavigation: (DietPreferencesNavigation) -> Unit) {
    composable<DietPreferencesRoute> {
        DietPreferencesScreen(onNavigation)
    }
}
