package pl.handsome.club.domain.analyze.macronutrient

sealed class MacronutrientAnalysisResult {

    object NoPreferences: MacronutrientAnalysisResult()

    class Success(
        val fatRate: KetoRate,
        val carbsRate: KetoRate
    ) : MacronutrientAnalysisResult()

}
