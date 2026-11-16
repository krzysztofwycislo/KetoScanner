package app.carbpeek.feature.onboarding.ui.state

import app.carbpeek.feature.onboarding.ui.OnboardingResources
import app.carbpeek.feature.preferences.api.model.DietPreset
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import app.carbpeek.feature.onboarding.ui.state.OnboardingStatePrototype as Prototype

internal class OnboardingStateFactory(private val resources: OnboardingResources) {

    fun createFor(prototype: Prototype): OnboardingState = OnboardingState(
        options = prototype.options(),
        isSaving = prototype.isSaving,
    )

    private fun Prototype.options(): PersistentList<PresetOptionState> =
        DietPreset.entries.map { preset ->
            PresetOptionState(
                preset = preset,
                title = resources.title(preset),
                note = resources.note(preset),
                limit = resources.limit(preset.preferences.maxDailyCarbsAmount),
                badge = preset.badge(),
                isSelected = preset == selectedPreset,
            )
        }.toPersistentList()

    private fun DietPreset.badge() = when (this) {
        DietPreset.KETO -> resources.suggested()
        else -> null
    }
}
