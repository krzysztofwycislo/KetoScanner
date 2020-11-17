package pl.handsome.club.domain.analyze.macronutrient

data class MacronutrientAnalysisResult(
    val carbsRate: DietRate,
    val maxRateCarbAmount: Int,
    val maxProductPortion: Double,
    val dailyCarbConsumption: Int
)
