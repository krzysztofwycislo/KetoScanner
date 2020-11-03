package pl.handsome.club.domain.analyze.macronutrient

import pl.handsome.club.domain.preferences.MacronutrientPreferences
import pl.handsome.club.domain.product.ProductNutriments


//TODO verification and adjustment
internal object MacronutrientAnalyzer {

    fun analyze(
        preferences: MacronutrientPreferences,
        productNutriments: ProductNutriments
    ): MacronutrientAnalysisResult {
        val fatRating = analyzeFat(productNutriments)
        val carbsRating = analyzeCarbs(productNutriments, preferences)

        return MacronutrientAnalysisResult.Success(fatRating, carbsRating)
    }

    private fun analyzeFat(
        productNutriments: ProductNutriments
    ): KetoRate {
        val fatToOverallRatio = with(productNutriments) {
            val allPerServing = carbohydratesPerServing + fatPerServing + proteinsPerServing
            fatPerServing / allPerServing
        }

        if (fatToOverallRatio >= .5) {
            return KetoRate.HIGH
        }

        if (fatToOverallRatio >= 0.3) {
            return KetoRate.MEDIUM
        }

        return KetoRate.LOW
    }

    //TODO verification and adjustment
    private fun analyzeCarbs(
        productNutriments: ProductNutriments,
        preferences: MacronutrientPreferences
    ): KetoRate {
        val maxCarbohydratesAmount = preferences.maxCarbohydratesAmount
        val carbohydratesPerServing = productNutriments.carbohydratesPerServing

        if (carbohydratesPerServing <= maxCarbohydratesAmount / 3) {
            return KetoRate.HIGH
        }

        if (carbohydratesPerServing <= maxCarbohydratesAmount / 2) {
            return KetoRate.MEDIUM
        }

        return KetoRate.LOW
    }


}
