package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.RatingReason

private const val CARB_SHARE_MIN_CARBS = 4.0

internal fun rateAtMost(value: Double, good: Double, neutral: Double): DietRate = when {
    value <= good -> DietRate.GOOD
    value <= neutral -> DietRate.NEUTRAL
    else -> DietRate.NOT_ADVISED
}

internal fun rateAtLeast(value: Double, good: Double, neutral: Double): DietRate = when {
    value >= good -> DietRate.GOOD
    value >= neutral -> DietRate.NEUTRAL
    else -> DietRate.NOT_ADVISED
}

internal fun NutritionProfile.hasRelevantCarbs(): Boolean = carbs > CARB_SHARE_MIN_CARBS

internal fun NutritionProfile.carbsReason(goodLimit: Double, neutralLimit: Double): RatingReason? =
    rateAtMost(carbs, goodLimit, neutralLimit).notGood()?.let { RatingReason.HighCarbs(carbs, it) }

internal fun NutritionProfile.carbEnergyShareReason(good: Double, neutral: Double): RatingReason? {
    if (!hasRelevantCarbs()) return null
    return rateAtMost(carbEnergyShare, good, neutral).notGood()
        ?.let { RatingReason.HighCarbEnergyShare(carbEnergyShare, it) }
}

internal fun NutritionProfile.sugarsReason(good: Double, neutral: Double): RatingReason? {
    val sugars = sugars ?: return null
    return rateAtMost(sugars, good, neutral).notGood()?.let { RatingReason.HighSugars(sugars, it) }
}

internal fun NutritionProfile.carbsPerServingReason(goodLimit: Double, neutralLimit: Double): RatingReason? {
    val servingGrams = servingGrams ?: return null
    val carbsPerServing = carbsPerServing ?: return null
    return rateAtMost(carbsPerServing, goodLimit, neutralLimit).notGood()
        ?.let { RatingReason.HighCarbsPerServing(servingGrams, carbsPerServing, it) }
}

internal fun DietRate.notGood(): DietRate? = takeUnless { it == DietRate.GOOD }
