package pl.handsome.club.ketoscanner.viewmodel.analyze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.fold
import pl.handsome.club.feature.analysis.api.AnalyzeProduct


class AnalyzeProductViewModel(
    private val analyzeProduct: AnalyzeProduct
) : ViewModel() {

    private val productAnalysisState = MutableLiveData<ProductAnalysisState>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        ProductAnalysisState.Error(AppError.Unknown(throwable)).let(productAnalysisState::postValue)
    }


    fun getProductAnalysisState(): LiveData<ProductAnalysisState> = productAnalysisState

    fun searchAndAnalyzeProduct(barcode: String) {
        if (productAnalysisState.value is ProductAnalysisState.InProgress) return
        productAnalysisState.value = ProductAnalysisState.InProgress

        viewModelScope.launch(coroutineExceptionHandler) {
            analyzeProduct(barcode)
                .fold(
                    onSuccess = { ProductAnalysisState.Success(barcode, it) },
                    onFailure = ::toFailureState
                )
                .let(productAnalysisState::postValue)
        }
    }

    private fun toFailureState(error: AppError): ProductAnalysisState = when (error) {
        AppError.NotFound -> ProductAnalysisState.ProductNotFound
        else -> ProductAnalysisState.Error(error)
    }

}
