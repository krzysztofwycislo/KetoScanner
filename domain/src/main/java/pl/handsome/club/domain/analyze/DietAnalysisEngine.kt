package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.product.Product


class DietAnalysisEngine {

    fun analyze(preferences: DietPreferences, product: Product): ProductAnalysisResult {
        val generalAnalysisResult = generalAnalyze(preferences, product)
        val ingredientAnalysisResult = ingredientAnalyze(preferences, product)
        val macronutrientAnalysisResult = macronutrientAnalyze(preferences, product)

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
        return GeneralAnalyser.analyze(preferences.kcalPerDay, product)
    }

    private fun ingredientAnalyze(
        preferences: DietPreferences,
        product: Product
    ): IngredientAnalysisResult {
        return preferences.ingredientPreferences
            ?.let { IngredientAnalyser.analyze(it, product) }
            ?: IngredientAnalysisResult.NoPreferences
    }

    private fun macronutrientAnalyze(
        preferences: DietPreferences,
        product: Product
    ): MacronutrientAnalysisResult {
        return preferences.macronutrientPreferences
            ?.let { MacronutrientAnalyser.analyze(it, product) }
            ?: MacronutrientAnalysisResult.NoPreferences
    }

}
