package pl.handsome.club.ketoscanner.viewmodel.favourite.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapLatest
import pl.handsome.club.domain.repository.FavouriteProductsRepository


@FlowPreview
@ExperimentalCoroutinesApi
class FavouriteProductsListViewModel(
    private val favouriteProductsRepository: FavouriteProductsRepository
) : ViewModel() {

    private val nameSearchChannel = ConflatedBroadcastChannel<String>().apply { offer("") }

    var favouriteProductsLiveData = nameSearchChannel.asFlow()
        .flatMapLatest(favouriteProductsRepository::search)
        .asLiveData()


    fun searchFavouriteProductsByName(name: String) {
        nameSearchChannel.offer(name)
    }

}