package pl.handsome.club.feature.analysis.api

import pl.handsome.club.feature.analysis.api.model.CarbLimits
import pl.handsome.club.feature.preferences.api.model.DietPreferences

interface DietRatingLimits {
    fun carbsPer100g(preferences: DietPreferences): CarbLimits
}
