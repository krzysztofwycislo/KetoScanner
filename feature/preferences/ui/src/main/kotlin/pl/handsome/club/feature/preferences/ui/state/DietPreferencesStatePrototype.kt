package pl.handsome.club.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.core.common.AppError
import pl.handsome.club.feature.preferences.api.model.DietMode

@Immutable
internal data class DietPreferencesStatePrototype(
    val savedAmount: Int? = null,
    val savedMode: DietMode? = null,
    val input: String? = null,
    val isInputInvalid: Boolean = false,
    val isSaving: Boolean = false,
    val loadError: AppError? = null,
) {

    companion object {
        val Default = DietPreferencesStatePrototype()
    }
}
