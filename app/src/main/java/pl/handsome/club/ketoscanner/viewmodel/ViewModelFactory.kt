package pl.handsome.club.ketoscanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.handsome.club.domain.analyze.DietAnalysisEngine
import pl.handsome.club.domain.repository.AnalysisHistoryRepository
import pl.handsome.club.domain.repository.DietPreferencesRepository
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.ketoscanner.repository.DBAnalysisHistoryRepository
import pl.handsome.club.ketoscanner.repository.DBDietPreferencesRepository
import pl.handsome.club.openfoodapi.OpenFoodFactsRepository

// TODO add dependency injection
object ViewModelFactory : ViewModelProvider.Factory {

    private val dietAnalysisEngine: DietAnalysisEngine = DietAnalysisEngine()

    private val productRepository: ProductRepository = OpenFoodFactsRepository()
    private val preferencesRepository: DietPreferencesRepository = DBDietPreferencesRepository()
    private val analysisHistoryRepository: AnalysisHistoryRepository = DBAnalysisHistoryRepository()


    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val viewModel = when (modelClass) {
            AnalyzeProductViewModel::class.java -> AnalyzeProductViewModel(
                dietAnalysisEngine,
                preferencesRepository,
                productRepository,
                analysisHistoryRepository
            )

            AnalysisHistoryViewModel::class.java -> AnalysisHistoryViewModel(
                analysisHistoryRepository
            )

            else -> modelClass.newInstance()
        }

        return viewModel as T
    }

}