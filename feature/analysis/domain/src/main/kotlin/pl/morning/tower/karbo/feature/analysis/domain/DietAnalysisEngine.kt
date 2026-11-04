package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.analysis.api.model.RatingReason
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.model.Product


internal class DietAnalysisEngine {

    fun analyze(preferences: DietPreferences, product: Product): ProductAnalysis {
        val profile = product.toNutritionProfile() ?: return ProductAnalysis.InsufficientData(product)
        val dailyCarbs = preferences.maxDailyCarbsAmount
        val reasons = scorerFor(preferences.dietMode).reasons(profile, dailyCarbs)
            .sortedByDescending(RatingReason::rate)

        return ProductAnalysis.Rated(
            product,
            MacronutrientAnalysisResult(
                dietMode = preferences.dietMode,
                rate = reasons.firstOrNull()?.rate ?: DietRate.GOOD,
                reasons = reasons,
                carbsPer100g = profile.carbs,
                maxProductPortion = if (profile.carbs > 0) dailyCarbs / profile.carbs * 100 else null,
                dailyCarbConsumption = dailyCarbs,
            ),
        )
    }

}
