package app.carbpeek.feature.favourites.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import app.carbpeek.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation
import kotlinx.serialization.Serializable

@Serializable
data object FavouritesRoute

fun NavGraphBuilder.favouritesDestination(onNavigation: (FavouritesNavigation) -> Unit) {
    composable<FavouritesRoute> {
        FavouritesScreen(onNavigation)
    }
}
