package app.carbpeek.feature.preferences.ui.state

import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset

internal enum class DietProfileOption(val preferences: DietPreferences) {
    STRICT_KETO(DietPreset.STRICT_KETO.preferences),
    KETO(DietPreset.KETO.preferences),
    LOW_CARB(DietPreset.LOW_CARB.preferences),
    NO_DIET(DietPreferences.NoDiet),
    ;

    companion object {
        fun of(preferences: DietPreferences): DietProfileOption {
            val preset = DietPreset.of(preferences) ?: return NO_DIET
            return entries.first { it.preferences == preset.preferences }
        }
    }
}
