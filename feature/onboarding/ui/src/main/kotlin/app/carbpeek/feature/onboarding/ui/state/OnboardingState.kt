package app.carbpeek.feature.onboarding.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.preferences.api.model.DietPreset
import kotlinx.collections.immutable.PersistentList

@Immutable
internal data class OnboardingState(
    val options: PersistentList<PresetOptionState>,
    val isSaving: Boolean,
)

@Immutable
internal data class PresetOptionState(
    val preset: DietPreset,
    val title: UiText,
    val note: UiText,
    val limit: UiText,
    val badge: UiText?,
    val isSelected: Boolean,
) {
    companion object
}
