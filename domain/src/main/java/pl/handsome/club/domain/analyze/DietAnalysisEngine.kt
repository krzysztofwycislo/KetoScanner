package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.analyze.general.GeneralAnalyzer
import pl.handsome.club.domain.analyze.general.GeneralAnalysisResult
import pl.handsome.club.domain.analyze.ingredient.IngredientAnalyzer
import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalyzer
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.product.ProductNutriments


class DietAnalysisEngine {

    fun analyze(preferences: DietPreferences, product: Product): ProductAnalysisResult {
        val generalAnalysisResult = generalAnalyze(preferences, product)
        val ingredientAnalysisResult = ingredientAnalyze(preferences, product)
        val macronutrientAnalysisResult = macronutrientAnalyze(preferences, product.nutriments)

        return ProductAnalysisResult(
            product,
            generalAnalysisResult,
            ingredientAnalysisResult,
            macronutrientAnalysisResult
        )
    }

    private fun generalAnalyze(
        preferences: DietPreferences,
        product: Product
    ): GeneralAnalysisResult {
        return GeneralAnalyzer.analyze(preferences.kcalPerDay, product)
    }

    private fun ingredientAnalyze(
        preferences: DietPreferences,
        product: Product
    ): IngredientAnalysisResult {
        return preferences.ingredientPreferences
            ?.let { IngredientAnalyzer.analyze(it, product) }
            ?: IngredientAnalysisResult.NoPreferences
    }

    private fun macronutrientAnalyze(
        preferences: DietPreferences,
        productNutriments: ProductNutriments
    ): MacronutrientAnalysisResult {
        return preferences.macronutrientPreferences
            ?.let { MacronutrientAnalyzer.analyze(it, productNutriments) }
            ?: MacronutrientAnalysisResult.NoPreferences
    }

}
