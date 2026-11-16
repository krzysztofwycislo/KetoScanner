package app.carbpeek.feature.favourites.ui.event

sealed interface FavouritesEvent {

    sealed interface FavouritesNavigation : FavouritesEvent {
        data class NavigateToProduct(val barcode: String) : FavouritesNavigation
        data object NavigateToScan : FavouritesNavigation
    }
}
