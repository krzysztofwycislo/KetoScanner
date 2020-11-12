package pl.handsome.club.domain.analyze.macronutrient

import pl.handsome.club.domain.preferences.MacronutrientPreferences
import pl.handsome.club.domain.product.ProductNutriments


internal object MacronutrientAnalyzer {

    fun analyze(
        preferences: MacronutrientPreferences,
        productNutriments: ProductNutriments
    ): MacronutrientAnalysisResult {
        val maxCarbohydratesAmount = preferences.maxCarbohydratesAmount
        val carbohydratesPerServing = productNutriments.carbohydratesPerServing

        val highRateCarbAmount = maxCarbohydratesAmount / 3
        if (carbohydratesPerServing <= highRateCarbAmount) {
            return MacronutrientAnalysisResult(DietRate.GOOD, highRateCarbAmount)
        }

        val neutralRateCarbAmount = maxCarbohydratesAmount / 2
        if (carbohydratesPerServing <= neutralRateCarbAmount) {
            return MacronutrientAnalysisResult(DietRate.NEUTRAL, neutralRateCarbAmount)
        }

        return MacronutrientAnalysisResult(DietRate.NOT_ADVISED, maxCarbohydratesAmount)
    }

}
