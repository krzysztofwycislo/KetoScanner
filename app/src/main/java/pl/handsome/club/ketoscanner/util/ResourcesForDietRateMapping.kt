package pl.handsome.club.ketoscanner.util

import pl.handsome.club.domain.analyze.macronutrient.DietRate
import pl.handsome.club.ketoscanner.R


fun getColorIdForDietRate(dietRate: DietRate) = dietRateColorsMap.getValue(dietRate)
private val dietRateColorsMap = mapOf(
    Pair(DietRate.GOOD, R.color.positive_result),
    Pair(DietRate.NEUTRAL, R.color.neutral_result),
    Pair(DietRate.NOT_ADVISED, R.color.negative_result)
)


fun getSummaryResultImageIdForDietRate(dietRate: DietRate?) =
    summaryResultImageMap.getOrDefault(dietRate, R.drawable.question_outline_negative_24dp)
private val summaryResultImageMap = mapOf(
    Pair(DietRate.GOOD, R.drawable.face_happy_green_24dp),
    Pair(DietRate.NEUTRAL, R.drawable.face_neutral_yellow_24dp),
    Pair(DietRate.NOT_ADVISED, R.drawable.face_sad_red_24dp)
)


fun getSummaryTextIdForDietRate(dietRate: DietRate) = dietRateSummaryMap.getValue(dietRate)
private val dietRateSummaryMap = mapOf(
    Pair(DietRate.GOOD, R.string.good_for_preferences_summary),
    Pair(DietRate.NEUTRAL, R.string.neutral_for_preferences_summary),
    Pair(DietRate.NOT_ADVISED, R.string.not_advised_for_preferences_summary)
)


fun getDetailTextIdForDietRate(carbsRate: DietRate): Int = dietRateDetailsMap.getValue(carbsRate)
private val dietRateDetailsMap = mapOf(
    Pair(DietRate.GOOD, R.string.good_for_preferences_details),
    Pair(DietRate.NEUTRAL, R.string.neutral_for_preferences_details),
    Pair(DietRate.NOT_ADVISED, R.string.not_advised_for_preferences_details)
)


fun getMaxServingImageIdForDietRate(dietRate: DietRate) =
    maxServingDietRateImageMap.getValue(dietRate)

private val maxServingDietRateImageMap = mapOf(
    Pair(DietRate.GOOD, R.drawable.local_pizza_positive_24dp),
    Pair(DietRate.NEUTRAL, R.drawable.local_pizza_neutral_24dp),
    Pair(DietRate.NOT_ADVISED, R.drawable.local_pizza_negative_24dp)
)
