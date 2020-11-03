package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.handsome.club.domain.analyze.DietAnalysisEngine
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.repository.DietPreferencesRepository
import pl.handsome.club.domain.analyze.ProductAnalysisState


class AnalyzeProductViewModel(
    private val dietAnalysisEngine: DietAnalysisEngine,
    private val preferencesRepository: DietPreferencesRepository
) : ViewModel() {

    private val productAnalysisState = MutableLiveData<ProductAnalysisState>()


    fun getProductAnalysisState(): LiveData<ProductAnalysisState> = productAnalysisState


    fun analyzeProduct(product: Product) {
        if(productAnalysisState.value is ProductAnalysisState.InProgress) return
        productAnalysisState.value = ProductAnalysisState.InProgress

        viewModelScope.launch(Dispatchers.IO) {
            val preferences = preferencesRepository.getDietPreferences()

            dietAnalysisEngine.analyze(preferences, product)
                .let(ProductAnalysisState::Success)
                .also(productAnalysisState::postValue)
        }
    }

}