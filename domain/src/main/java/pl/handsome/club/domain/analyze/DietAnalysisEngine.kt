package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.ingredient.IngredientAnalyzer
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalyzer
import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


open class DietAnalysisEngine {

    open fun analyze(preferences: DietPreferences, product: Product): ProductAnalysisResult {
        val ingredientAnalysisResult = ingredientAnalyze(preferences, product)
        val macronutrientAnalysisResult = macronutrientAnalyze(preferences, product.nutriments)

        return ProductAnalysisResult(
            product,
            macronutrientAnalysisResult,
            ingredientAnalysisResult
        )
    }

    private fun ingredientAnalyze(
        preferences: DietPreferences,
        product: Product
    ): IngredientAnalysisResult? {
        return preferences.ingredientPreferences
            ?.let { IngredientAnalyzer.analyze(it, product) }
    }

    private fun macronutrientAnalyze(
        preferences: DietPreferences,
        productNutriments: ProductNutriments
    ): MacronutrientAnalysisResult {
        return preferences.macronutrientPreferences
            .let { MacronutrientAnalyzer.analyze(it, productNutriments) }
    }

}
