package pl.handsome.club.domain.analyze.macronutrient

sealed class MacronutrientAnalysisResult {

    object NoPreferences: MacronutrientAnalysisResult()
    class Error(val throwable: Throwable) : MacronutrientAnalysisResult()
    class Success() : MacronutrientAnalysisResult()

}
