package pl.handsome.club.feature.preferences.api.model


data class DietPreferences(
    val maxDailyCarbsAmount: Int
) {

    companion object {
        val Default = DietPreferences(40)
        val MAX_DAILY_CARBS_RANGE = 1..999
    }
}
