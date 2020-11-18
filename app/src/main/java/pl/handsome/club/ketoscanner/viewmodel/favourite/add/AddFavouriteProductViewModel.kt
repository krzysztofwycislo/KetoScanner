package pl.handsome.club.ketoscanner.viewmodel.favourite.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.repository.FavouriteProductsRepository

class AddFavouriteProductViewModel(
    private val favouriteProductsRepository: FavouriteProductsRepository
) : ViewModel() {

    private val addToFavouritesState = MutableLiveData<AddToFavouritesState>()
    private val isProductInFavourites = MutableLiveData<Boolean>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AddToFavouritesState.Error(throwable).also(addToFavouritesState::postValue)
    }


    fun getAddToFavouritesState(): LiveData<AddToFavouritesState> = addToFavouritesState
    fun getIsProductInFavourites(): LiveData<Boolean> = isProductInFavourites

    fun addOrRemoveFromFavourites(product: Product) {
        addToFavouritesState.value = AddToFavouritesState.InProgress

        viewModelScope.launch(coroutineExceptionHandler) {
            val favouriteProductCheck = favouriteProductsRepository.findByBarcode(product.barcode)

            if (favouriteProductCheck == null) {
                addToFavourites(product)
            } else {
                removeFromFavourites(product)
            }
        }
    }

    private suspend fun removeFromFavourites(product: Product) {
        favouriteProductsRepository.removeFromFavourites(product)
        isProductInFavourites.postValue(false)
        addToFavouritesState.postValue(AddToFavouritesState.Removed)
    }

    private suspend fun addToFavourites(product: Product) {
        favouriteProductsRepository.addToFavourites(product)
        isProductInFavourites.postValue(true)
        addToFavouritesState.postValue(AddToFavouritesState.Success)
    }

    fun isProductInFavourites(product: Product) {
        viewModelScope.launch {
            val productCheck = favouriteProductsRepository.findByBarcode(product.barcode)
            isProductInFavourites.postValue(productCheck != null)
        }
    }

}