package pl.handsome.club.ketoscanner.viewmodel.analyze

import pl.handsome.club.feature.analysis.api.model.ProductAnalysisResult

sealed class ProductAnalysisState {

    object InProgress : ProductAnalysisState()
    object ProductNotFound : ProductAnalysisState()
    data class Success(val result: ProductAnalysisResult) : ProductAnalysisState()
    data class Error(val throwable: Throwable) : ProductAnalysisState()

}
