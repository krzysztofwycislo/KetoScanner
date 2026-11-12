package pl.morning.tower.karbo.feature.analysis.domain

import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.RatingReason

private const val CARB_SHARE_MIN_CARBS = 4.0
private const val LIGHT_CARBS_SUGARS_MAX = 7.0
private const val WHOLE_PLANT_FIBRE_SHARE_MIN = 0.15
private const val WHOLE_PLANT_SUGARS_SHARE_MIN = 0.3

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
    rateAtMost(value = carbs, good = goodLimit, neutral = neutralLimit)
        .notGood()
        ?.let { RatingReason.HighCarbs(gramsPer100g = carbs, rate = it) }

internal fun NutritionProfile.hasLightCarbs(portionCarbsGood: Double): Boolean {
    val sugars = sugars ?: return false
    return sugars <= LIGHT_CARBS_SUGARS_MAX && maxOf(carbsPerServing ?: carbs, carbs) <= portionCarbsGood
}

internal fun NutritionProfile.isWholePlantFood(): Boolean {
    if (liquid || carbs <= 0) return false
    val fibre = fibre ?: return false
    val sugars = sugars ?: return false
    return fibre / carbs >= WHOLE_PLANT_FIBRE_SHARE_MIN && sugars / carbs >= WHOLE_PLANT_SUGARS_SHARE_MIN
}

internal fun NutritionProfile.carbEnergyShareReason(
    good: Double,
    neutral: Double,
    capAtNeutral: Boolean,
): RatingReason? {
    if (!hasRelevantCarbs()) return null
    val rate = rateAtMost(value = carbEnergyShare, good = good, neutral = neutral)
    return (if (capAtNeutral) rate.atMostNeutral() else rate).notGood()
        ?.let { RatingReason.HighCarbEnergyShare(share = carbEnergyShare, rate = it) }
}

internal fun NutritionProfile.sugarsReason(good: Double, neutral: Double): RatingReason? {
    val sugars = sugars ?: return null
    return rateAtMost(value = sugars, good = good, neutral = neutral)
        .notGood()
        ?.let { RatingReason.HighSugars(gramsPer100g = sugars, rate = it) }
}

internal fun NutritionProfile.carbsPerServingReason(goodLimit: Double, neutralLimit: Double): RatingReason? {
    val servingGrams = servingGrams ?: return null
    val carbsPerServing = carbsPerServing ?: return null
    return rateAtMost(value = carbsPerServing, good = goodLimit, neutral = neutralLimit)
        .notGood()
        ?.let { RatingReason.HighCarbsPerServing(servingGrams = servingGrams, carbsGrams = carbsPerServing, rate = it) }
}

internal fun DietRate.atMostNeutral(): DietRate = if (this == DietRate.NOT_ADVISED) DietRate.NEUTRAL else this

internal fun DietRate.notGood(): DietRate? = takeUnless { it == DietRate.GOOD }
