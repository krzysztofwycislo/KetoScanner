package pl.morning.tower.karbo.feature.analysis.api.model

sealed interface RatingReason {

    val rate: DietRate

    data class HighCarbs(val gramsPer100g: Double, override val rate: DietRate) : RatingReason

    data class HighCarbEnergyShare(val share: Double, override val rate: DietRate) : RatingReason

    data class LowFatAndProteinToCarbRatio(val ratio: Double, override val rate: DietRate) : RatingReason

    data class HighSugars(val gramsPer100g: Double, override val rate: DietRate) : RatingReason

    data class HighCarbsPerServing(
        val servingGrams: Double,
        val carbsGrams: Double,
        override val rate: DietRate,
    ) : RatingReason
}
