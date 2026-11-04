package pl.morning.tower.karbo.feature.analysis.domain

import pl.morning.tower.karbo.feature.analysis.api.model.CarbLimits
import pl.morning.tower.karbo.feature.analysis.api.model.RatingReason

internal object LowCarbScorer : DietScorer {

    private const val CARBS_GOOD_LIMIT_DIVISOR = 3.0
    private const val CARBS_NEUTRAL_LIMIT_DIVISOR = 2.0
    private const val CARB_SHARE_GOOD = 0.25
    private const val CARB_SHARE_NEUTRAL = 0.50
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
            sugarsReason(SUGARS_GOOD, SUGARS_NEUTRAL),
            carbsPerServingReason(dailyCarbs / SERVING_GOOD_LIMIT_DIVISOR, dailyCarbs / SERVING_NEUTRAL_LIMIT_DIVISOR),
        )
    }
}
