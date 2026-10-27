package pl.handsome.club.ketoscanner.viewmodel.analyze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.AppLogger
import pl.handsome.club.core.common.onFailure
import pl.handsome.club.feature.analysis.domain.DietAnalysisEngine
import pl.handsome.club.feature.analysis.api.model.ProductAnalysisResult
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductSearchState
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.product.api.ProductRepository


class AnalyzeProductViewModel(
    private val dietAnalysisEngine: DietAnalysisEngine,
    private val preferencesRepository: DietPreferencesRepository,
    private val productRepository: ProductRepository,
    private val analysisHistoryRepository: AnalysisHistoryRepository,
    private val logger: AppLogger
) : ViewModel() {

    private val productAnalysisState = MutableLiveData<ProductAnalysisState>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        ProductAnalysisState.Error(throwable).let(productAnalysisState::postValue)
    }


    fun getProductAnalysisState(): LiveData<ProductAnalysisState> = productAnalysisState

    fun searchAndAnalyzeProduct(barcode: String) {
        if (productAnalysisState.value is ProductAnalysisState.InProgress) return
        productAnalysisState.value = ProductAnalysisState.InProgress

        viewModelScope.launch(coroutineExceptionHandler) {
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
        val result = analyzeProduct(product)
        analysisHistoryRepository.save(result).onFailure { error ->
            logger.error(TAG, "Saving history entry failed: $error", (error as? AppError.Unknown)?.cause)
        }
        return ProductAnalysisState.Success(result)
    }

    private suspend fun analyzeProduct(product: Product): ProductAnalysisResult {
        val preferences = preferencesRepository.getDietPreferences()
        return dietAnalysisEngine.analyze(preferences, product)
    }

    private companion object {
        const val TAG = "AnalyzeProductViewModel"
    }

}