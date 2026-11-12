package pl.morning.tower.karbo.feature.analysis.api.model

import pl.morning.tower.karbo.feature.preferences.api.model.DietMode

data class MacronutrientAnalysisResult(
    val dietMode: DietMode,
    val rate: DietRate,
    val reasons: List<RatingReason>,
    val carbsPer100g: Double,
    val dailyCarbConsumption: Int,
)
