package pl.handsome.club.feature.favourites.ui.state

import kotlinx.collections.immutable.toPersistentList
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.favourites.ui.FavouritesResources
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.Empty
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.Error
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.InitialLoad
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.NoResults
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState.ShowItems
import pl.handsome.club.feature.favourites.ui.state.FavouritesStatePrototype as Prototype

internal class FavouritesStateFactory(private val resources: FavouritesResources) {

    fun createFor(prototype: Prototype): FavouritesState = FavouritesState(
        query = prototype.query,
        content = prototype.content(),
    )

    private fun Prototype.content(): FavouritesContentState {
        val favourites = favourites
        val loadError = loadError
        return when {
            loadError != null -> Error(resources.loadError(loadError))
            favourites == null -> InitialLoad
            favourites.isEmpty() && favouritesQuery.isBlank() -> Empty
            favourites.isEmpty() -> NoResults(resources.noResults(favouritesQuery.trim()))
            else -> ShowItems(favourites.map { it.toItemState() }.toPersistentList())
        }
    }

    private fun FavouriteProduct.toItemState(): FavouriteItemState = FavouriteItemState(
        barcode = productBarcode,
        name = resources.productName(productName),
        brand = resources.brand(productBrand),
        imageUrl = imageUrl,
    )
}
