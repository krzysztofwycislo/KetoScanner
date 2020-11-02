package pl.handsome.club.domain.analyze

sealed class IngredientAnalysisResult {

    object NoPreferences: IngredientAnalysisResult()
    class Error(val throwable: Throwable) : IngredientAnalysisResult()
    class Success() : IngredientAnalysisResult()

}
