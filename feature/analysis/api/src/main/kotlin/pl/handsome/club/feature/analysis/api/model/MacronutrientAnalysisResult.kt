package pl.handsome.club.feature.analysis.api.model

data class MacronutrientAnalysisResult(
    val carbsRate: DietRate,
    val maxRateCarbAmount: Int,
    val maxProductPortion: Double,
    val dailyCarbConsumption: Int
)
