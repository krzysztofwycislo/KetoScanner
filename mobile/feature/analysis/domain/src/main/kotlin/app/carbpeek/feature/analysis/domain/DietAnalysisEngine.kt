package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.analysis.api.model.RatingReason
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.product.api.model.Product

internal class DietAnalysisEngine {

    fun analyze(preferences: DietPreferences, product: Product): ProductAnalysis {
        val profile = product.toNutritionProfile() ?: return ProductAnalysis.InsufficientData(product)
        val dailyCarbs = preferences.maxDailyCarbsAmount
        val reasons = scorerFor(preferences.dietMode).reasons(profile = profile, dailyCarbs = dailyCarbs)
            .sortedByDescending(RatingReason::rate)
        val rate = reasons.firstOrNull()?.rate ?: DietRate.GOOD

        return ProductAnalysis.Rated(
            product = product,
            macronutrients = MacronutrientAnalysisResult(
                dietMode = preferences.dietMode,
                rate = rate,
                reasons = reasons,
                carbsPer100g = profile.carbs,
                dailyCarbConsumption = dailyCarbs,
            ),
        )
    }
}
