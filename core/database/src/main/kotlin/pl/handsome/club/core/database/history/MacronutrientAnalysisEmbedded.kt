package pl.handsome.club.core.database.history

data class MacronutrientAnalysisEmbedded(
    val carbsRate: String,
    val maxRateCarbAmount: Int,
    val maxProductPortion: Double,
    val dailyCarbConsumption: Int
)
