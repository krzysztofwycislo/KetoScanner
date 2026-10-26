package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.model.ProductNutriments


internal object MacronutrientAnalyzer {

    fun analyze(
        preferences: DietPreferences,
        productNutriments: ProductNutriments
    ): MacronutrientAnalysisResult? {
        val carbohydratesPer100g = productNutriments.carbohydratesPer100g ?: return null

        val dailyCarbConsumption = preferences.maxDailyCarbsAmount
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
