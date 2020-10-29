package pl.handsome.club.ketoscanner.viewmodel.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pl.handsome.club.domain.repository.ProductsRepository


class SearchProductViewModel(
    private val productsRepository: ProductsRepository
) : ViewModel() {

    private val searchState = MutableLiveData<SearchState>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        SearchState.SearchingError(throwable).let(searchState::postValue)
    }

    fun getSearchState(): LiveData<SearchState> = searchState

    fun searchProductByBarcode(barcode: String) {
        if(searchState.value is SearchState.SearchingInProgress) return

        searchState.value = SearchState.SearchingInProgress

        viewModelScope.launch(coroutineExceptionHandler) {
            productsRepository.searchProductByBarcode(barcode)
                .let(SearchState::SearchingSuccess)
                .apply(searchState::postValue)
        }
    }

}