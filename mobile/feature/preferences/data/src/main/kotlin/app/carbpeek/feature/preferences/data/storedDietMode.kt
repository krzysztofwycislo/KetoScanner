package app.carbpeek.feature.preferences.data

import app.carbpeek.feature.preferences.api.model.DietPreferences

internal const val NO_DIET_MODE = "NONE"

internal fun DietPreferences.storedDietMode(): String = dietMode?.name ?: NO_DIET_MODE
