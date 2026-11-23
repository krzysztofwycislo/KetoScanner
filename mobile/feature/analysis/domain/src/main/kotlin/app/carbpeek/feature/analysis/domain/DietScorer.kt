package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.RatingReason
import app.carbpeek.feature.preferences.api.model.DietMode

internal interface DietScorer {
    fun reasons(profile: NutritionProfile, dailyCarbs: Int): List<RatingReason>
}

internal fun scorerFor(mode: DietMode): DietScorer = when (mode) {
    DietMode.KETO -> KetoScorer
    DietMode.LOW_CARB -> LowCarbScorer
}
