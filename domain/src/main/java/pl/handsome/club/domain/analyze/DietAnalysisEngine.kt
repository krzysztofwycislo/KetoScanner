package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalyzer
import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.product.Product


open class DietAnalysisEngine {

    open fun analyze(preferences: DietPreferences, product: Product): ProductAnalysisResult {
        val macronutrientAnalysisResult = MacronutrientAnalyzer.analyze(
            preferences,
            product.nutriments
        )

        return ProductAnalysisResult(
            product,
            macronutrientAnalysisResult
        )
    }

}
