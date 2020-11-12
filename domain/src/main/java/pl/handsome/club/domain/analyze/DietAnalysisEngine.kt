package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.analyze.ingredient.IngredientAnalyzer
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalyzer
import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.product.Product


open class DietAnalysisEngine {

    open fun analyze(preferences: DietPreferences, product: Product): ProductAnalysisResult {
        val macronutrientAnalysisResult = MacronutrientAnalyzer.analyze(
            preferences.macronutrientPreferences,
            product.nutriments
        )

        val ingredientAnalysisResult = IngredientAnalyzer.analyze(
            preferences.ingredientPreferences,
            product
        )

        return ProductAnalysisResult(
            product,
            macronutrientAnalysisResult,
            ingredientAnalysisResult
        )
    }

}
