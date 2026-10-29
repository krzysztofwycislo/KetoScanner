package pl.handsome.club.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.core.common.AppError

@Immutable
internal data class DietPreferencesStatePrototype(
    val savedAmount: Int? = null,
    val input: String? = null,
    val isInputInvalid: Boolean = false,
    val loadError: AppError? = null,
) {

    companion object {
        val Default = DietPreferencesStatePrototype()
    }
}
