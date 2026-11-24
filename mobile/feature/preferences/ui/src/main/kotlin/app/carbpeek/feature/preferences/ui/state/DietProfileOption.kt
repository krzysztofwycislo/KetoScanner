package app.carbpeek.feature.preferences.ui.state

import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset

internal enum class DietProfileOption(val preset: DietPreset) {
    STRICT_KETO(DietPreset.STRICT_KETO),
    KETO(DietPreset.KETO),
    LOW_CARB(DietPreset.LOW_CARB),
    ;

    companion object {
        fun of(preferences: DietPreferences): DietProfileOption {
            val preset = DietPreset.of(preferences)
            return entries.first { it.preset == preset }
        }
    }
}
