package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.domain.product.ProductSearchState


class SearchProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val productSearchState = MutableLiveData<ProductSearchState>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        ProductSearchState.Error(throwable).let(productSearchState::postValue)
    }

    fun getProductSearchState(): LiveData<ProductSearchState> = productSearchState

    fun searchProductByBarcode(barcode: String) {
        if(productSearchState.value is ProductSearchState.InProgress) return
        productSearchState.value = ProductSearchState.InProgress

        viewModelScope.launch(coroutineExceptionHandler) {
            productRepository.searchProductByBarcode(barcode)
                .apply(productSearchState::postValue)
        }
    }

}