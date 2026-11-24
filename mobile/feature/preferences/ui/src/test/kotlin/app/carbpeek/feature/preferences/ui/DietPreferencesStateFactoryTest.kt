package app.carbpeek.feature.preferences.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.preferences.ui.state.DietPreferencesContentState
import app.carbpeek.feature.preferences.ui.state.DietPreferencesStateFactory
import app.carbpeek.feature.preferences.ui.state.DietProfileOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import app.carbpeek.core.designsystem.R as DesignR
import app.carbpeek.feature.preferences.ui.state.DietPreferencesStatePrototype as Prototype

class DietPreferencesStateFactoryTest {

    private val factory = DietPreferencesStateFactory(DietPreferencesResources())

    @Test
    fun `when prototype is default then content is initial load`() {
        assertEquals(DietPreferencesContentState.InitialLoad, factory.createFor(Prototype.Default).content)
    }

    @Test
    fun `when only an option is restored then content is still initial load`() {
        assertEquals(
            DietPreferencesContentState.InitialLoad,
            factory.createFor(Prototype(option = DietProfileOption.LOW_CARB)).content,
        )
    }

    @Test
    fun `when load error is set then content is error even if preferences were loaded`() {
        val state = factory.createFor(loaded(DietPreferences(40)).copy(loadError = AppError.Storage))

        assertEquals(
            DietPreferencesContentState.Error(UiText.Res(DesignR.string.designsystem_error_storage)),
            state.content,
        )
    }

    @Test
    fun `presets match their own profile and other profiles become keto`() {
        assertEquals(DietPreset.STRICT_KETO, DietPreset.of(DietPreferences(20, DietMode.KETO)))
        assertEquals(DietPreset.KETO, DietPreset.of(DietPreferences(40, DietMode.KETO)))
        assertEquals(DietPreset.LOW_CARB, DietPreset.of(DietPreferences(100, DietMode.LOW_CARB)))
        listOf(
            DietPreferences(maxDailyCarbsAmount = 40, dietMode = DietMode.LOW_CARB),
            DietPreferences(maxDailyCarbsAmount = 35, dietMode = DietMode.KETO),
        ).forEach { preferences ->
            assertEquals(DietPreset.KETO, DietPreset.of(preferences))
            assertEquals(DietProfileOption.KETO, DietProfileOption.of(preferences))
        }
    }

    @Test
    fun `no diet is not a preset`() {
        assertNull(DietPreset.of(DietPreferences.NoDiet))
        assertNull(DietPreset.of(DietPreferences(maxDailyCarbsAmount = 100, dietMode = null)))
        assertEquals(
            listOf(DietPreset.STRICT_KETO, DietPreset.KETO, DietPreset.LOW_CARB),
            DietPreset.entries.toList(),
        )
    }

    @Test
    fun `each preset option is selected for its stored profile and the others are not`() {
        mapOf(
            DietPreferences(20, DietMode.KETO) to DietProfileOption.STRICT_KETO,
            DietPreferences(40, DietMode.KETO) to DietProfileOption.KETO,
            DietPreferences(100, DietMode.LOW_CARB) to DietProfileOption.LOW_CARB,
            DietPreferences.NoDiet to DietProfileOption.NO_DIET,
        ).forEach { (preferences, expected) ->
            val options = form(loaded(preferences)).options

            assertEquals(listOf(expected), options.filter { it.isSelected }.map { it.option })
        }
    }

    @Test
    fun `when the stored profile matches no preset then keto is selected`() {
        val options = form(loaded(DietPreferences(35, DietMode.KETO))).options

        assertEquals(listOf(DietProfileOption.KETO), options.filter { it.isSelected }.map { it.option })
    }

    @Test
    fun `options are listed in order with titles notes and preset amounts`() {
        val options = form(loaded(DietPreferences(40))).options

        assertEquals(
            listOf(
                DietProfileOption.STRICT_KETO,
                DietProfileOption.KETO,
                DietProfileOption.LOW_CARB,
                DietProfileOption.NO_DIET,
            ),
            options.map { it.option },
        )
        assertEquals(UiText.Res(R.string.preferences_preset_strict_keto), options[0].title)
        assertEquals(UiText.Res(R.string.preferences_preset_strict_keto_note), options[0].note)
        assertEquals(grams(20), options[0].trailing)
        assertEquals(grams(40), options[1].trailing)
        assertEquals(grams(100), options[2].trailing)
        assertEquals(UiText.Res(R.string.preferences_no_diet), options[3].title)
        assertEquals(UiText.Res(R.string.preferences_no_diet_note), options[3].note)
        assertNull(options[3].trailing)
    }

    @Test
    fun `when saving then save is disabled otherwise enabled`() {
        assertFalse(form(loaded(DietPreferences(40)).copy(isSaving = true)).isSaveEnabled)
        assertTrue(form(loaded(DietPreferences(40))).isSaveEnabled)
    }

    private fun loaded(preferences: DietPreferences) = Prototype(
        savedPreferences = preferences,
        option = DietProfileOption.of(preferences),
    )

    private fun form(prototype: Prototype): DietPreferencesContentState.Form =
        assertIs<DietPreferencesContentState.Form>(factory.createFor(prototype).content)

    private fun grams(amount: Long) = UiText.Res(R.string.preferences_grams, UiArg.Numeric.Count(amount))
}
