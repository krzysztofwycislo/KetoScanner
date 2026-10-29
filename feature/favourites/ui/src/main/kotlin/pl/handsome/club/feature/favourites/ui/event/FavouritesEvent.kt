package pl.handsome.club.feature.favourites.ui.event

sealed interface FavouritesEvent {

    sealed interface FavouritesNavigation : FavouritesEvent {
        data class NavigateToProduct(val barcode: String) : FavouritesNavigation
    }
}
