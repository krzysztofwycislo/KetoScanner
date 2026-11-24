package app.carbpeek.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.common.AppError
import app.carbpeek.feature.preferences.api.model.DietPreferences

@Immutable
internal data class DietPreferencesStatePrototype(
    val savedPreferences: DietPreferences? = null,
    val option: DietProfileOption? = null,
    val isSaving: Boolean = false,
    val loadError: AppError? = null,
) {

    fun chosenPreferences(): DietPreferences? = option?.preset?.preferences

    companion object {
        val Default = DietPreferencesStatePrototype()
    }
}
