package pl.morning.tower.karbo.feature.preferences.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation

@Serializable
data object DietPreferencesRoute

fun NavGraphBuilder.dietPreferencesDestination(onNavigation: (DietPreferencesNavigation) -> Unit) {
    composable<DietPreferencesRoute> {
        DietPreferencesScreen(onNavigation)
    }
}
