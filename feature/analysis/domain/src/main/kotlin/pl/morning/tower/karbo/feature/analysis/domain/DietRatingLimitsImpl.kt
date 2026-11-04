package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.DietRatingLimits
import pl.handsome.club.feature.analysis.api.model.CarbLimits
import pl.handsome.club.feature.preferences.api.model.DietPreferences

internal class DietRatingLimitsImpl : DietRatingLimits {
    override fun carbsPer100g(preferences: DietPreferences): CarbLimits =
        scorerFor(preferences.dietMode).carbsLimits(preferences.maxDailyCarbsAmount)
}
