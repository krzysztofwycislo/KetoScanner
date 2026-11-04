package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.CarbLimits
import pl.handsome.club.feature.analysis.api.model.RatingReason

internal object KetoScorer : DietScorer {

    private const val CARBS_GOOD_LIMIT_DIVISOR = 3.0
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
        CarbLimits(dailyCarbs / CARBS_GOOD_LIMIT_DIVISOR, dailyCarbs / CARBS_NEUTRAL_LIMIT_DIVISOR)

    override fun reasons(profile: NutritionProfile, dailyCarbs: Int): List<RatingReason> = with(profile) {
        val carbsLimits = carbsLimits(dailyCarbs)
        listOfNotNull(
            carbsReason(carbsLimits.goodMax, carbsLimits.neutralMax),
            carbEnergyShareReason(CARB_SHARE_GOOD, CARB_SHARE_NEUTRAL),
            fatAndProteinToCarbRatioReason(),
            sugarsReason(SUGARS_GOOD, SUGARS_NEUTRAL),
            carbsPerServingReason(dailyCarbs / SERVING_GOOD_LIMIT_DIVISOR, dailyCarbs / SERVING_NEUTRAL_LIMIT_DIVISOR),
        )
    }

    private fun NutritionProfile.fatAndProteinToCarbRatioReason(): RatingReason? {
        if (!hasRelevantCarbs()) return null
        return rateAtLeast(
            fatAndProteinToCarbRatio,
            FAT_AND_PROTEIN_TO_CARB_GOOD,
            FAT_AND_PROTEIN_TO_CARB_NEUTRAL,
        ).notGood()
            ?.let { RatingReason.LowFatAndProteinToCarbRatio(fatAndProteinToCarbRatio, it) }
    }
}
