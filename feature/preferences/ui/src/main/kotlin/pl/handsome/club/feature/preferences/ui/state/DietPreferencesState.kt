package pl.handsome.club.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.core.designsystem.text.UiText

@Immutable
internal data class DietPreferencesState(
    val content: DietPreferencesContentState,
)

@Immutable
internal sealed interface DietPreferencesContentState {
    data object InitialLoad : DietPreferencesContentState
    data class Error(val message: UiText) : DietPreferencesContentState
    data class Form(val summary: UiText, val input: String, val inputError: UiText?) : DietPreferencesContentState
}
