package app.carbpeek.feature.preferences.api.model

enum class DietPreset(val preferences: DietPreferences) {
    STRICT_KETO(DietPreferences(20, DietMode.KETO)),
    KETO(DietPreferences(40, DietMode.KETO)),
    LOW_CARB(DietPreferences(100, DietMode.LOW_CARB)),
    ;

    companion object {
        fun of(preferences: DietPreferences): DietPreset? = entries.firstOrNull { it.preferences == preferences }
    }
}
