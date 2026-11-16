package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.DietRatingLimits
import app.carbpeek.feature.analysis.api.model.CarbLimits
import app.carbpeek.feature.preferences.api.model.DietPreferences

internal class DietRatingLimitsImpl : DietRatingLimits {
    override fun carbsPer100g(preferences: DietPreferences): CarbLimits =
        scorerFor(preferences.dietMode).carbsLimits(preferences.maxDailyCarbsAmount)
}
