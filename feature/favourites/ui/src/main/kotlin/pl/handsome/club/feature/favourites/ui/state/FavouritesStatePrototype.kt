package pl.handsome.club.feature.favourites.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.core.common.AppError
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct

@Immutable
internal data class FavouritesStatePrototype(
    val query: String = "",
    val favourites: List<FavouriteProduct>? = null,
    val favouritesQuery: String = "",
    val loadError: AppError? = null,
) {

    companion object {
        val Default = FavouritesStatePrototype()
    }
}
