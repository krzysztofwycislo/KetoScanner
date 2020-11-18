package pl.handsome.club.ketoscanner.viewmodel.favourite.add


sealed class AddToFavouritesState {

    object InProgress : AddToFavouritesState()
    data class Error(val throwable: Throwable) : AddToFavouritesState()
    object Success : AddToFavouritesState()
    object Removed : AddToFavouritesState()

}
