package pl.handsome.club.feature.favourites.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.model.ProductCategory
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.preferences.api.model.DietPreferences

@Immutable
internal data class FavouritesStatePrototype(
    val query: String = "",
    val favourites: List<FavouriteProduct>? = null,
    val favouritesQuery: String = "",
    val loadError: AppError? = null,
    val dietPreferences: DietPreferences? = null,
    val collapsed: Set<ProductCategory> = emptySet(),
) {

    companion object {
        val Default = FavouritesStatePrototype()
    }
}
