package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.CarbLimits
import app.carbpeek.feature.analysis.api.model.RatingReason

internal object KetoScorer : DietScorer {

    private const val CARBS_GOOD_LIMIT_DIVISOR = 4.0
    private const val CARBS_NEUTRAL_LIMIT_DIVISOR = 2.0
    private const val CARB_SHARE_GOOD = 0.10
    private const val CARB_SHARE_NEUTRAL = 0.25
    private const val FAT_AND_PROTEIN_TO_CARB_GOOD = 2.0
    private const val FAT_AND_PROTEIN_TO_CARB_NEUTRAL = 1.0
    private const val SUGARS_GOOD = 5.0
    private const val SUGARS_NEUTRAL = 10.0
    private const val SERVING_GOOD_LIMIT_DIVISOR = 4.0
    private const val SERVING_NEUTRAL_LIMIT_DIVISOR = 2.0

    override fun carbsLimits(dailyCarbs: Int): CarbLimits =
        CarbLimits(
            goodMax = dailyCarbs / CARBS_GOOD_LIMIT_DIVISOR,
            neutralMax = dailyCarbs / CARBS_NEUTRAL_LIMIT_DIVISOR,
        )

    override fun reasons(profile: NutritionProfile, dailyCarbs: Int): List<RatingReason> = with(profile) {
        val carbsLimits = carbsLimits(dailyCarbs)
        val lightCarbs = hasLightCarbs(portionCarbsGood = dailyCarbs / SERVING_GOOD_LIMIT_DIVISOR)
        val wholePlantFood = isWholePlantFood()
        listOfNotNull(
            carbsReason(
                goodLimit = carbsLimits.goodMax,
                neutralLimit = carbsLimits.neutralMax,
            ),
            carbEnergyShareReason(
                good = if (lightCarbs) CARB_SHARE_NEUTRAL else CARB_SHARE_GOOD,
                neutral = CARB_SHARE_NEUTRAL,
                capAtNeutral = lightCarbs || wholePlantFood,
            ),
            fatAndProteinToCarbRatioReason(
                lightCarbs = lightCarbs,
                capAtNeutral = lightCarbs || wholePlantFood,
            ),
            sugarsReason(
                good = SUGARS_GOOD,
                neutral = SUGARS_NEUTRAL,
            ),
            carbsPerServingReason(
                goodLimit = dailyCarbs / SERVING_GOOD_LIMIT_DIVISOR,
                neutralLimit = dailyCarbs / SERVING_NEUTRAL_LIMIT_DIVISOR,
            ),
        )
    }

    private fun NutritionProfile.fatAndProteinToCarbRatioReason(
        lightCarbs: Boolean,
        capAtNeutral: Boolean,
    ): RatingReason? {
        if (!hasRelevantCarbs()) return null
        val rate = rateAtLeast(
            value = fatAndProteinToCarbRatio,
            good = if (lightCarbs) FAT_AND_PROTEIN_TO_CARB_NEUTRAL else FAT_AND_PROTEIN_TO_CARB_GOOD,
            neutral = FAT_AND_PROTEIN_TO_CARB_NEUTRAL,
        )
        return (if (capAtNeutral) rate.atMostNeutral() else rate).notGood()
            ?.let { RatingReason.LowFatAndProteinToCarbRatio(ratio = fatAndProteinToCarbRatio, rate = it) }
    }
}
