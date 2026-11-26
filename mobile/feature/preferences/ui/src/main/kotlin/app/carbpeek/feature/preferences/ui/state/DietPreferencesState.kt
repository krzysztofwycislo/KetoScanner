package app.carbpeek.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.designsystem.text.UiText
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
        val detailsSwitch: DetailsSwitchState?,
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
internal data class DetailsSwitchState(val isOn: Boolean)
