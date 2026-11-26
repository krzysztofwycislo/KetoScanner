package app.carbpeek.feature.preferences.api.model

data class ProfileSettings(
    val dietPreferences: DietPreferences,
    val opensDetailsRightAway: Boolean = false,
) {

    val showsQuickCard: Boolean
        get() = dietPreferences.dietMode != null && !opensDetailsRightAway

    companion object {
        val Default = ProfileSettings(dietPreferences = DietPreferences.Default)
    }
}
