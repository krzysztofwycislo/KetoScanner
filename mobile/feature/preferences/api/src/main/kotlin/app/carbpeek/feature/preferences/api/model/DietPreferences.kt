package app.carbpeek.feature.preferences.api.model


data class DietPreferences(
    val maxDailyCarbsAmount: Int,
    val dietMode: DietMode = DietMode.KETO,
) {

    companion object {
        val Default = DietPreferences(40)
        val MAX_DAILY_CARBS_RANGE = 1..999
    }
}
