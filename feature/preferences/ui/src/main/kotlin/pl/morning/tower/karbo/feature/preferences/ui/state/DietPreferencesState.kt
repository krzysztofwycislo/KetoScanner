package pl.morning.tower.karbo.feature.preferences.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode

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
        val ratingGuide: RatingGuideState?,
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

@Immutable
internal data class RatingGuideState(
    val subtitle: UiText,
    val rows: PersistentList<RatingGuideRowState>,
    val footnote: UiText,
)

@Immutable
internal data class RatingGuideRowState(
    val rating: Rating,
    val label: UiText,
    val value: UiText,
) {
    companion object
}
