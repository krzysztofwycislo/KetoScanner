package pl.handsome.club.feature.favourites.ui.state

import kotlinx.collections.immutable.toPersistentList
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.icon.icon
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.favourites.ui.FavouritesResources
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.Empty
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.Error
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.InitialLoad
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.NoResults
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.ShowItems
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.favourites.ui.state.FavouritesStatePrototype as Prototype

internal class FavouritesStateFactory(private val resources: FavouritesResources) {

    fun createFor(prototype: Prototype): FavouritesState {
        val mode = (prototype.dietPreferences ?: DietPreferences.Default).dietMode
        val content = prototype.content(mode)
        return FavouritesState(
            query = prototype.query,
            showSearch = content is ShowItems || content is NoResults,
            content = content,
        )
    }

    private fun Prototype.content(mode: DietMode): FavouritesContentState {
        val favourites = favourites
        val loadError = loadError
        return when {
            loadError != null -> Error(resources.loadError(loadError))
            favourites == null -> InitialLoad
            favourites.isEmpty() && favouritesQuery.isBlank() -> Empty
            favourites.isEmpty() -> NoResults(resources.noResults(favouritesQuery.trim()))
            else -> ShowItems(favourites.map { it.toItemState(mode) }.toPersistentList())
        }
    }

    private fun FavouriteProduct.toItemState(mode: DietMode): FavouriteItemState = FavouriteItemState(
        barcode = productBarcode,
        name = resources.productName(productName),
        icon = category.icon,
        brand = resources.brand(productBrand),
        rating = dietRate.toRating(),
        carbs = resources.carbs(carbsPer100g),
        verdictDescription = resources.verdictDescription(dietRate, mode, carbsPer100g),
    )

    private fun DietRate?.toRating(): Rating = when (this) {
        DietRate.GOOD -> Rating.Positive
        DietRate.NEUTRAL -> Rating.Neutral
        DietRate.NOT_ADVISED -> Rating.Negative
        null -> Rating.Unknown
    }
}
