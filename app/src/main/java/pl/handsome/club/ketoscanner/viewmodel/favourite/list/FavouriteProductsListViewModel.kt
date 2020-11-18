package pl.handsome.club.ketoscanner.viewmodel.favourite.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import kotlinx.coroutines.launch
import pl.handsome.club.domain.product.FavouriteProduct
import pl.handsome.club.domain.repository.FavouriteProductsRepository


class FavouriteProductsListViewModel(
    private val favouriteProductsRepository: FavouriteProductsRepository
) : ViewModel() {

    private lateinit var favouriteProductsLiveData: LiveData<PagedList<FavouriteProduct>>

    init {
        viewModelScope.launch {
            favouriteProductsLiveData = favouriteProductsRepository.load().toLiveData(PAGE_SIZE)
        }
    }

    fun getFavouriteProducts() = favouriteProductsLiveData


    companion object {
        const val PAGE_SIZE = 10
    }
}