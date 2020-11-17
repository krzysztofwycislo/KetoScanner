package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.repository.FavouriteProductsRepository

class AddFavouriteProductViewModel (
    private val favouriteProducts: FavouriteProductsRepository
) : ViewModel() {

    private val addToFavouritesState = MutableLiveData<AddToFavouritesState>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AddToFavouritesState.Error(throwable).also(addToFavouritesState::postValue)
    }


    fun getAddToFavouritesState(): LiveData<AddToFavouritesState> = addToFavouritesState

    fun addToFavourites(product: Product) {
        addToFavouritesState.value = AddToFavouritesState.InProgress

        viewModelScope.launch(coroutineExceptionHandler) {
            favouriteProducts.addToFavourites(product)
            addToFavouritesState.postValue(AddToFavouritesState.Success)
        }
    }

}