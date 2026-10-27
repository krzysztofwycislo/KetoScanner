package pl.handsome.club.ketoscanner.viewmodel.analyze

import pl.handsome.club.core.common.AppError
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis

sealed class ProductAnalysisState {

    object InProgress : ProductAnalysisState()
    object ProductNotFound : ProductAnalysisState()
    data class Success(val barcode: String, val analysis: ProductAnalysis) : ProductAnalysisState()
    data class Error(val error: AppError) : ProductAnalysisState()

}
