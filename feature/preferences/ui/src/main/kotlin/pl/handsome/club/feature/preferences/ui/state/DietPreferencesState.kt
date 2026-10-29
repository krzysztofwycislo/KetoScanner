package pl.handsome.club.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.preferences.api.model.DietMode

@Immutable
internal data class DietPreferencesState(
    val content: DietPreferencesContentState,
)

@Immutable
internal sealed interface DietPreferencesContentState {
    data object InitialLoad : DietPreferencesContentState
    data class Error(val message: UiText) : DietPreferencesContentState
    data class Form(
        val dietModes: PersistentList<DietModeOptionState>,
        val summary: UiText,
        val input: String,
        val inputError: UiText?,
    ) : DietPreferencesContentState
}

@Immutable
internal data class DietModeOptionState(
    val mode: DietMode,
    val label: UiText,
    val description: UiText,
    val isSelected: Boolean,
)
