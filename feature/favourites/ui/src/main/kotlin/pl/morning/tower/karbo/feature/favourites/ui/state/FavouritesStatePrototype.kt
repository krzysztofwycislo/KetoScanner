package pl.morning.tower.karbo.feature.favourites.ui.state

import androidx.compose.runtime.Immutable
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences

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
