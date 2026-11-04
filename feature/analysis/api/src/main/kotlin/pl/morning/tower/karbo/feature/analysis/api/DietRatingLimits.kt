package pl.morning.tower.karbo.feature.analysis.api

import pl.morning.tower.karbo.feature.analysis.api.model.CarbLimits
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences

interface DietRatingLimits {
    fun carbsPer100g(preferences: DietPreferences): CarbLimits
}
