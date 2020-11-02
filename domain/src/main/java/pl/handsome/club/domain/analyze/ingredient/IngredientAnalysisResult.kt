package pl.handsome.club.domain.analyze.ingredient

sealed class IngredientAnalysisResult {

    object NoPreferences: IngredientAnalysisResult()
    class Error(val throwable: Throwable) : IngredientAnalysisResult()
    class Success() : IngredientAnalysisResult()

}
