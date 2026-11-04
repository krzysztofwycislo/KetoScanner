package pl.morning.tower.karbo.feature.favourites.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.morning.tower.karbo.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation

@Serializable
data object FavouritesRoute

fun NavGraphBuilder.favouritesDestination(onNavigation: (FavouritesNavigation) -> Unit) {
    composable<FavouritesRoute> {
        FavouritesScreen(onNavigation)
    }
}
