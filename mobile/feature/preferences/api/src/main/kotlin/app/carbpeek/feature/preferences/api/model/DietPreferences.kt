package app.carbpeek.feature.preferences.api.model


data class DietPreferences(
    val maxDailyCarbsAmount: Int,
    val dietMode: DietMode? = DietMode.KETO,
) {

    companion object {
        val Default = DietPreferences(40)
        val NoDiet = DietPreferences(maxDailyCarbsAmount = Default.maxDailyCarbsAmount, dietMode = null)
    }
}
