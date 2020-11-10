package pl.handsome.club.ketoscanner.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.handsome.club.domain.analyze.DietAnalysisEngine
import pl.handsome.club.domain.analyze.ProductAnalysisResult
import pl.handsome.club.domain.analyze.ProductAnalysisState
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductSearchState
import pl.handsome.club.domain.repository.AnalysisHistoryRepository
import pl.handsome.club.domain.repository.DietPreferencesRepository
import pl.handsome.club.domain.repository.ProductRepository


class AnalyzeProductViewModel(
    private val dietAnalysisEngine: DietAnalysisEngine,
    private val preferencesRepository: DietPreferencesRepository,
    private val productRepository: ProductRepository,
    private val analysisHistoryRepository: AnalysisHistoryRepository
) : ViewModel() {

    private val productAnalysisState = MutableLiveData<ProductAnalysisState>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        ProductAnalysisState.Error(throwable).let(productAnalysisState::postValue)
    }


    fun getProductAnalysisState(): LiveData<ProductAnalysisState> = productAnalysisState

    fun searchAndAnalyzeProduct(barcode: String) {
        if (productAnalysisState.value is ProductAnalysisState.InProgress) return
        productAnalysisState.value = ProductAnalysisState.InProgress

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            productRepository.searchProductByBarcode(barcode)
                .also { handleSearchResult(it) }
        }
    }

    private suspend fun handleSearchResult(searchResult: ProductSearchState) {
        val analysisState = when (searchResult) {
            is ProductSearchState.Success -> onSearchResultSuccess(searchResult.product)
            is ProductSearchState.Error -> ProductAnalysisState.Error(searchResult.throwable)
            is ProductSearchState.NotFound -> ProductAnalysisState.ProductNotFound
        }

        productAnalysisState.postValue(analysisState)
    }

    private suspend fun onSearchResultSuccess(product: Product): ProductAnalysisState {
        Log.i("TEST", Thread.currentThread().name)
        return analyzeProduct(product)
            .also { analysisHistoryRepository.save(it) }
            .let { ProductAnalysisState.Success(it) }
    }

    private fun analyzeProduct(product: Product): ProductAnalysisResult {
        val preferences = preferencesRepository.getDietPreferences()
        return dietAnalysisEngine.analyze(preferences, product)
    }

}