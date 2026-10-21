package pl.handsome.club.ketoscanner.viewmodel.favourite.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import pl.handsome.club.domain.repository.FavouriteProductsRepository


@ExperimentalCoroutinesApi
class FavouriteProductsListViewModel(
    private val favouriteProductsRepository: FavouriteProductsRepository
) : ViewModel() {

    private val nameSearch = MutableStateFlow("")

    var favouriteProductsLiveData = nameSearch
        .flatMapLatest { favouriteProductsRepository.search(it) }
        .asLiveData()


    fun searchFavouriteProductsByName(name: String) {
        nameSearch.value = name
    }

}
