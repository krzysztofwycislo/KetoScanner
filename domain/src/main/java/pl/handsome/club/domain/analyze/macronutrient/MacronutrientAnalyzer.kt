package pl.handsome.club.domain.analyze.macronutrient

import pl.handsome.club.domain.preferences.MacronutrientPreferences
import pl.handsome.club.domain.product.ProductNutriments


internal object MacronutrientAnalyzer {

    fun analyze(
        preferences: MacronutrientPreferences,
        productNutriments: ProductNutriments
    ): MacronutrientAnalysisResult? {
        val carbohydratesPer100g = productNutriments.carbohydratesPer100g
            ?: return null

        val dailyCarbConsumption = preferences.maxCarbohydratesAmount
        val maxProductAmount = dailyCarbConsumption / carbohydratesPer100g * 100

        val highRateCarbAmount = dailyCarbConsumption / 3
        if (carbohydratesPer100g <= highRateCarbAmount) {
            return MacronutrientAnalysisResult(
                DietRate.GOOD,
                highRateCarbAmount,
                maxProductAmount,
                dailyCarbConsumption
            )
        }

        val neutralRateCarbAmount = dailyCarbConsumption / 2
        if (carbohydratesPer100g <= neutralRateCarbAmount) {
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

}
