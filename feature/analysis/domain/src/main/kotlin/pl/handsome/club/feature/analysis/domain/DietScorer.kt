package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.RatingReason

internal fun interface DietScorer {
    fun reasons(profile: NutritionProfile, dailyCarbs: Int): List<RatingReason>
}
