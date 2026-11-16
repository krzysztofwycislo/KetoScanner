package app.carbpeek.feature.analysis.api

import app.carbpeek.feature.analysis.api.model.CarbLimits
import app.carbpeek.feature.preferences.api.model.DietPreferences

interface DietRatingLimits {
    fun carbsPer100g(preferences: DietPreferences): CarbLimits
}
