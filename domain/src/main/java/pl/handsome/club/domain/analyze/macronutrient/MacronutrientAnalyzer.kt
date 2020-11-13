package pl.handsome.club.domain.analyze.macronutrient

import pl.handsome.club.domain.preferences.MacronutrientPreferences
import pl.handsome.club.domain.product.ProductNutriments


internal object MacronutrientAnalyzer {

    fun analyze(
        preferences: MacronutrientPreferences,
        productNutriments: ProductNutriments
    ): MacronutrientAnalysisResult {
        val dailyCarbConsumption = preferences.maxCarbohydratesAmount
        val carbohydratesPerServing = productNutriments.carbohydratesPerServing

        val maxProductAmount = getMaxProductAmount(productNutriments, dailyCarbConsumption)

        val highRateCarbAmount = dailyCarbConsumption / 3
        if (carbohydratesPerServing <= highRateCarbAmount) {
            return MacronutrientAnalysisResult(
                DietRate.GOOD,
                highRateCarbAmount,
                maxProductAmount,
                dailyCarbConsumption
            )
        }

        val neutralRateCarbAmount = dailyCarbConsumption / 2
        if (carbohydratesPerServing <= neutralRateCarbAmount) {
            return MacronutrientAnalysisResult(
                DietRate.NEUTRAL,
                neutralRateCarbAmount,
                maxProductAmount,
                dailyCarbConsumption
            )
        }

        return MacronutrientAnalysisResult(
            DietRate.NOT_ADVISED,
            dailyCarbConsumption,
            maxProductAmount,
            dailyCarbConsumption
        )
    }

    private fun getMaxProductAmount(
        productNutriments: ProductNutriments,
        dailyCarbConsumption: Int
    ): Double {
        val carbohydratesPer100grams = productNutriments.carbohydratesPer100g

        return dailyCarbConsumption / carbohydratesPer100grams * 100
    }

}
