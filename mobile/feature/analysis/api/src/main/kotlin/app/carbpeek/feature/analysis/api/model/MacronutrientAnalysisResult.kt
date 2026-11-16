package app.carbpeek.feature.analysis.api.model

import app.carbpeek.feature.preferences.api.model.DietMode

data class MacronutrientAnalysisResult(
    val dietMode: DietMode,
    val rate: DietRate,
    val reasons: List<RatingReason>,
    val carbsPer100g: Double,
    val dailyCarbConsumption: Int,
)
