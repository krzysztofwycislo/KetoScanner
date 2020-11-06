package pl.handsome.club.domain.analyze


sealed class ProductAnalysisState {

    object InProgress : ProductAnalysisState()
    object ProductNotFound : ProductAnalysisState()
    data class Success(val result: ProductAnalysisResult) : ProductAnalysisState()
    data class Error(val throwable: Throwable) : ProductAnalysisState()

}
