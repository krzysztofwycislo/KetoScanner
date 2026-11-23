package app.carbpeek.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.preferences.api.model.DietMode
import kotlinx.collections.immutable.PersistentList

@Immutable
internal data class DietPreferencesState(
    val content: DietPreferencesContentState,
)

@Immutable
internal sealed interface DietPreferencesContentState {
    data object InitialLoad : DietPreferencesContentState
    data class Error(val message: UiText) : DietPreferencesContentState
    data class Form(
        val options: PersistentList<DietProfileOptionState>,
        val custom: CustomProfileState?,
        val isSaveEnabled: Boolean,
    ) : DietPreferencesContentState
}

@Immutable
internal data class DietProfileOptionState(
    val option: DietProfileOption,
    val title: UiText,
    val note: UiText,
    val trailing: UiText?,
    val isSelected: Boolean,
) {
    companion object
}

@Immutable
internal data class CustomProfileState(
    val modes: PersistentList<DietModeOptionState>,
    val input: String,
    val inputError: UiText?,
)

@Immutable
internal data class DietModeOptionState(
    val mode: DietMode,
    val label: UiText,
    val isSelected: Boolean,
) {
    companion object
}
