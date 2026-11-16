package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.CarbLimits
import app.carbpeek.feature.analysis.api.model.RatingReason

internal object LowCarbScorer : DietScorer {

    private const val CARBS_GOOD_LIMIT_DIVISOR = 4.0
    private const val CARBS_NEUTRAL_LIMIT_DIVISOR = 3.0
    private const val CARB_SHARE_MAX = 0.50
    private const val SUGARS_GOOD = 5.0
    private const val SUGARS_NEUTRAL = 15.0
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
        listOfNotNull(
            carbsReason(
                goodLimit = carbsLimits.goodMax,
                neutralLimit = carbsLimits.neutralMax,
            ),
            carbEnergyShareReason(
                good = CARB_SHARE_MAX,
                neutral = CARB_SHARE_MAX,
                capAtNeutral = lightCarbs || isWholePlantFood(),
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
}
