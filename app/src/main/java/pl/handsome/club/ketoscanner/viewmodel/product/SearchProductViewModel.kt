package pl.handsome.club.ketoscanner.viewmodel.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pl.handsome.club.domain.product.SearchProduct
import pl.handsome.club.domain.repository.ProductsRepository


class SearchProductViewModel(
    private val productsRepository: ProductsRepository
) : ViewModel() {

    private val searchState = MutableLiveData<SearchProduct>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        SearchProduct.Error(throwable).let(searchState::postValue)
    }

    fun getSearchState(): LiveData<SearchProduct> = searchState

    fun searchProductByBarcode(barcode: String) {
        if(searchState.value is SearchProduct.InProgress) return

        searchState.value = SearchProduct.InProgress

        viewModelScope.launch(coroutineExceptionHandler) {
            productsRepository.searchProductByBarcode(barcode)
                .apply(searchState::postValue)
        }
    }

}