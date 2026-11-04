package pl.morning.tower.karbo.feature.preferences.ui.state

import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset

internal enum class DietProfileOption(val preset: DietPreset?) {
    STRICT_KETO(DietPreset.STRICT_KETO),
    KETO(DietPreset.KETO),
    LOW_CARB(DietPreset.LOW_CARB),
    CUSTOM(null),
    ;

    companion object {
        fun of(preferences: DietPreferences): DietProfileOption {
            val preset = DietPreset.of(preferences) ?: return CUSTOM
            return entries.first { it.preset == preset }
        }
    }
}
