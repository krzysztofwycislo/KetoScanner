package app.carbpeek.feature.preferences.data

import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.preferences.api.model.ProfileSettings

internal const val NO_DIET_MODE = "NONE"

internal fun DietPreferences.storedDietMode(): String = dietMode?.name ?: NO_DIET_MODE

internal fun ProfileSettings.toStored(): StoredProfileSettings =
    StoredProfileSettings(
        maxDailyCarbsAmount = dietPreferences.maxDailyCarbsAmount,
        dietMode = dietPreferences.storedDietMode(),
        opensDetailsRightAway = opensDetailsRightAway,
    )

internal fun StoredDietPreferences.toDomain(): DietPreferences {
    val preset = DietPreset.of(
        DietPreferences(
            maxDailyCarbsAmount = maxDailyCarbsAmount ?: DietPreferences.Default.maxDailyCarbsAmount,
            dietMode = domainDietMode(),
        ),
    )
    return preset?.preferences ?: DietPreferences.NoDiet
}

private fun StoredDietPreferences.domainDietMode(): DietMode? =
    if (dietMode == NO_DIET_MODE) {
        null
    } else {
        DietMode.entries.firstOrNull { it.name == dietMode } ?: DietMode.KETO
    }
