package pl.morning.tower.karbo.feature.analysis.domain

import pl.morning.tower.karbo.feature.analysis.api.model.CarbLimits
import pl.morning.tower.karbo.feature.analysis.api.model.RatingReason
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode

internal interface DietScorer {
    fun carbsLimits(dailyCarbs: Int): CarbLimits

    fun reasons(profile: NutritionProfile, dailyCarbs: Int): List<RatingReason>
}

internal fun scorerFor(mode: DietMode): DietScorer = when (mode) {
    DietMode.KETO -> KetoScorer
    DietMode.LOW_CARB -> LowCarbScorer
}
