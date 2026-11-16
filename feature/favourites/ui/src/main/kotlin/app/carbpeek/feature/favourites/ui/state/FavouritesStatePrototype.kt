package app.carbpeek.feature.favourites.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.common.AppError
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.favourites.api.model.FavouriteProduct
import app.carbpeek.feature.preferences.api.model.DietPreferences

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
