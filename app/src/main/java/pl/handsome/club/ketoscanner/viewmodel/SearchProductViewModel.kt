package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pl.handsome.club.domain.data.Product
import pl.handsome.club.ketoscanner.repository.ProductsRepository


class SearchProductViewModel(
    private val productsRepository: ProductsRepository
) : ViewModel() {

    private val searchedProduct = MutableLiveData<Product>()


    fun getSearchedProduct(): LiveData<Product> = searchedProduct


    fun searchProductByBarcode(barcode: String) = viewModelScope.launch {
        productsRepository.searchProductByBarcode(barcode)
            .apply(searchedProduct::postValue)
    }

}