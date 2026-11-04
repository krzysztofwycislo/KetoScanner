package pl.morning.tower.karbo.feature.analysis.domain

import pl.morning.tower.karbo.feature.analysis.api.DietRatingLimits
import pl.morning.tower.karbo.feature.analysis.api.model.CarbLimits
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences

internal class DietRatingLimitsImpl : DietRatingLimits {
    override fun carbsPer100g(preferences: DietPreferences): CarbLimits =
        scorerFor(preferences.dietMode).carbsLimits(preferences.maxDailyCarbsAmount)
}
