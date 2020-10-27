package pl.handsome.club.ketoscanner.viewmodel.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import pl.handsome.club.domain.repository.ProductsRepository
import pl.handsome.club.ketoscanner.viewmodel.launchOnIO
import pl.handsome.club.ketoscanner.viewmodel.logException


class SearchProductViewModel(
    private val productsRepository: ProductsRepository
) : ViewModel() {

    private val searchState = MutableLiveData<SearchState>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logException(throwable)
        SearchState.SearchingError(throwable)
            .let(searchState::postValue)
    }

    fun getSearchState(): LiveData<SearchState> = searchState

    fun searchProductByBarcode(barcode: String) {
        searchState.value = SearchState.SearchingInProgress

        launchOnIO(coroutineExceptionHandler) {
            productsRepository.searchProductByBarcode(barcode)
                .let(SearchState::SearchingSuccess)
                .apply(searchState::postValue)
        }
    }

}