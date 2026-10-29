package pl.handsome.club.feature.favourites.ui.event

internal sealed interface FavouritesAction {
    data class OnQueryChanged(val query: String) : FavouritesAction
    data object OnSearchSubmitted : FavouritesAction
    data class OnItemClicked(val barcode: String) : FavouritesAction
    data object OnRetryClicked : FavouritesAction
}
