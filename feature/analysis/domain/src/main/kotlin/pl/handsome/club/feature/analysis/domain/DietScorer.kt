package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.CarbLimits
import pl.handsome.club.feature.analysis.api.model.RatingReason
import pl.handsome.club.feature.preferences.api.model.DietMode

internal interface DietScorer {
    fun carbsLimits(dailyCarbs: Int): CarbLimits

    fun reasons(profile: NutritionProfile, dailyCarbs: Int): List<RatingReason>
}

internal fun scorerFor(mode: DietMode): DietScorer = when (mode) {
    DietMode.KETO -> KetoScorer
    DietMode.LOW_CARB -> LowCarbScorer
}
