package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.handsome.club.domain.analyze.DietAnalysisEngine
import pl.handsome.club.domain.repository.DietPreferencesRepository
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.ketoscanner.repository.DBDietPreferencesRepository
import pl.handsome.club.openfoodapi.OpenFoodFactsRepository

// TODO add dependency injection
object ViewModelFactory : ViewModelProvider.Factory {

    private val productRepository: ProductRepository = OpenFoodFactsRepository()
    private val dietAnalysisEngine: DietAnalysisEngine = DietAnalysisEngine()
    private val preferencesRepository: DietPreferencesRepository = DBDietPreferencesRepository()


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val viewModel = when (modelClass) {

            SearchProductViewModel::class.java -> SearchProductViewModel(productRepository)

            AnalyzeProductViewModel::class.java -> AnalyzeProductViewModel(
                dietAnalysisEngine,
                preferencesRepository
            )

            else -> modelClass.newInstance()
        }

        return viewModel as T
    }

}