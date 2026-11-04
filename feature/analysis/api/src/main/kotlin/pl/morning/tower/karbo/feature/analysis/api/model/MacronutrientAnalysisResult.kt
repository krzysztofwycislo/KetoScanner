package pl.handsome.club.feature.analysis.api.model

import pl.handsome.club.feature.preferences.api.model.DietMode

data class MacronutrientAnalysisResult(
    val dietMode: DietMode,
    val rate: DietRate,
    val reasons: List<RatingReason>,
    val carbsPer100g: Double,
    val maxProductPortion: Double?,
    val dailyCarbConsumption: Int,
)
