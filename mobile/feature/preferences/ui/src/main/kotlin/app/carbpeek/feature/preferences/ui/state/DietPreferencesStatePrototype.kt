package app.carbpeek.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.common.AppError
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.ProfileSettings

@Immutable
internal data class DietPreferencesStatePrototype(
    val savedPreferences: DietPreferences? = null,
    val option: DietProfileOption? = null,
    val opensDetailsRightAway: Boolean? = null,
    val isSaving: Boolean = false,
    val loadError: AppError? = null,
) {

    fun chosenSettings(): ProfileSettings? {
        if (option == null || opensDetailsRightAway == null) return null
        return ProfileSettings(
            dietPreferences = option.preferences,
            opensDetailsRightAway = opensDetailsRightAway,
        )
    }

    companion object {
        val Default = DietPreferencesStatePrototype()
    }
}
