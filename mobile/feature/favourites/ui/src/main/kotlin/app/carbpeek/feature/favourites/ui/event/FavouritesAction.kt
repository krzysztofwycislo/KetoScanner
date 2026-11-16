package app.carbpeek.feature.favourites.ui.event

import app.carbpeek.core.model.ProductCategory

internal sealed interface FavouritesAction {
    data class OnQueryChanged(val query: String) : FavouritesAction
    data object OnSearchSubmitted : FavouritesAction
    data class OnItemClicked(val barcode: String) : FavouritesAction
    data object OnRetryClicked : FavouritesAction
    data object OnStartScanningClicked : FavouritesAction
    data class OnGroupClicked(val category: ProductCategory) : FavouritesAction
}
