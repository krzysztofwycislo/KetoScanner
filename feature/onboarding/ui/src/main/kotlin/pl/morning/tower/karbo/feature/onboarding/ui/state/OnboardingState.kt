package pl.morning.tower.karbo.feature.onboarding.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset

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
