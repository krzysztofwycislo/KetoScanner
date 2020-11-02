package pl.handsome.club.domain.analyze.macronutrient

import pl.handsome.club.domain.preferences.MacronutrientPreferences
import pl.handsome.club.domain.product.Product

internal object MacronutrientAnalyser {

    fun analyze(preferences: MacronutrientPreferences, product: Product) : MacronutrientAnalysisResult {
        return MacronutrientAnalysisResult.Success()
    }

}
