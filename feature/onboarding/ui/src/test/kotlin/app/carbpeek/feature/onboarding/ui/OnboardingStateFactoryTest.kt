package app.carbpeek.feature.onboarding.ui

import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.onboarding.ui.state.OnboardingStateFactory
import app.carbpeek.feature.onboarding.ui.state.OnboardingStatePrototype
import app.carbpeek.feature.preferences.api.model.DietPreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingStateFactoryTest {

    private val factory = OnboardingStateFactory(OnboardingResources())

    @Test
    fun `when created then there is one option per preset in enum order with its title and note`() {
        val options = factory.createFor(OnboardingStatePrototype()).options

        assertEquals(DietPreset.entries, options.map { it.preset })
        assertEquals(
            listOf(
                UiText.Res(R.string.onboarding_preset_strict_keto),
                UiText.Res(R.string.onboarding_preset_keto),
                UiText.Res(R.string.onboarding_preset_low_carb),
            ),
            options.map { it.title },
        )
        assertEquals(
            listOf(
                UiText.Res(R.string.onboarding_preset_strict_keto_note),
                UiText.Res(R.string.onboarding_preset_keto_note),
                UiText.Res(R.string.onboarding_preset_low_carb_note),
            ),
            options.map { it.note },
        )
    }

    @Test
    fun `when created then only keto has the good start badge`() {
        val options = factory.createFor(OnboardingStatePrototype()).options

        assertEquals(
            listOf(null, UiText.Res(R.string.onboarding_suggested), null),
            options.map { it.badge },
        )
    }

    @Test
    fun `when created then limits are 20, 40 and 100 g`() {
        val options = factory.createFor(OnboardingStatePrototype()).options

        assertEquals(
            listOf(20L, 40L, 100L).map { UiText.Res(R.string.onboarding_grams, UiArg.Numeric.Count(it)) },
            options.map { it.limit },
        )
    }

    @Test
    fun `when a preset is selected then it is the only selected option`() {
        DietPreset.entries.forEach { preset ->
            val options = factory.createFor(OnboardingStatePrototype(selectedPreset = preset)).options

            assertEquals(listOf(preset), options.filter { it.isSelected }.map { it.preset })
        }
    }

    @Test
    fun `when saving then saving passes through`() {
        assertTrue(factory.createFor(OnboardingStatePrototype(isSaving = true)).isSaving)
        assertFalse(factory.createFor(OnboardingStatePrototype(isSaving = false)).isSaving)
    }
}
