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
import kotlin.test.assertNotNull
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
    fun `when only input is restored then content is still initial load`() {
        assertEquals(DietPreferencesContentState.InitialLoad, factory.createFor(Prototype(input = "25")).content)
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
    fun `presets match only their own profile`() {
        assertEquals(DietPreset.STRICT_KETO, DietPreset.of(DietPreferences(20, DietMode.KETO)))
        assertEquals(DietPreset.KETO, DietPreset.of(DietPreferences(40, DietMode.KETO)))
        assertEquals(DietPreset.LOW_CARB, DietPreset.of(DietPreferences(100, DietMode.LOW_CARB)))
        assertNull(DietPreset.of(DietPreferences(40, DietMode.LOW_CARB)))
        assertNull(DietPreset.of(DietPreferences(35, DietMode.KETO)))
        assertEquals(DietProfileOption.CUSTOM, DietProfileOption.of(DietPreferences(40, DietMode.LOW_CARB)))
        assertEquals(DietProfileOption.CUSTOM, DietProfileOption.of(DietPreferences(35, DietMode.KETO)))
    }

    @Test
    fun `each preset option is selected for its stored profile and the others are not`() {
        mapOf(
            DietPreferences(20, DietMode.KETO) to DietProfileOption.STRICT_KETO,
            DietPreferences(40, DietMode.KETO) to DietProfileOption.KETO,
            DietPreferences(100, DietMode.LOW_CARB) to DietProfileOption.LOW_CARB,
        ).forEach { (preferences, expected) ->
            val options = form(loaded(preferences)).options

            assertEquals(listOf(expected), options.filter { it.isSelected }.map { it.option })
        }
    }

    @Test
    fun `when the stored profile matches no preset then custom is selected`() {
        val options = form(loaded(DietPreferences(35, DietMode.KETO))).options

        assertEquals(listOf(DietProfileOption.CUSTOM), options.filter { it.isSelected }.map { it.option })
    }

    @Test
    fun `options are listed in order with titles notes and preset amounts`() {
        val options = form(loaded(DietPreferences(40))).options

        assertEquals(DietProfileOption.entries, options.map { it.option })
        assertEquals(UiText.Res(R.string.preferences_preset_strict_keto), options[0].title)
        assertEquals(UiText.Res(R.string.preferences_preset_strict_keto_note), options[0].note)
        assertEquals(grams(20), options[0].trailing)
        assertEquals(grams(40), options[1].trailing)
        assertEquals(grams(100), options[2].trailing)
        assertEquals(UiText.Res(R.string.preferences_preset_custom), options[3].title)
    }

    @Test
    fun `custom trailing text is shown only for a valid number`() {
        val base = loaded(DietPreferences(40)).copy(option = DietProfileOption.CUSTOM)

        assertEquals(grams(35), form(base.copy(input = "35")).options.last().trailing)
        assertNull(form(base.copy(input = "")).options.last().trailing)
        assertNull(form(base.copy(input = "0")).options.last().trailing)
        assertNull(form(base.copy(input = "1000")).options.last().trailing)
    }

    @Test
    fun `custom fields are hidden for presets and shown for custom with the selected mode`() {
        assertNull(form(loaded(DietPreferences(40))).custom)

        val custom = assertNotNull(form(loaded(DietPreferences(80, DietMode.LOW_CARB))).custom)

        assertEquals(DietMode.entries, custom.modes.map { it.mode })
        assertEquals(DietMode.LOW_CARB, custom.modes.single { it.isSelected }.mode)
        assertEquals(UiText.Res(R.string.preferences_diet_mode_low_carb), custom.modes[1].label)
        assertEquals("80", custom.input)
        assertNull(custom.inputError)
    }

    @Test
    fun `input error is shown only when input is invalid`() {
        val custom = assertNotNull(
            form(loaded(DietPreferences(35)).copy(input = "0", isInputInvalid = true)).custom,
        )

        assertEquals(
            UiText.Res(R.string.preferences_input_error, UiArg.Numeric.Count(1), UiArg.Numeric.Count(999)),
            custom.inputError,
        )
    }

    @Test
    fun `when saving then save is disabled otherwise enabled`() {
        assertFalse(form(loaded(DietPreferences(40)).copy(isSaving = true)).isSaveEnabled)
        assertTrue(form(loaded(DietPreferences(40))).isSaveEnabled)
    }

    private fun loaded(preferences: DietPreferences) = Prototype(
        savedPreferences = preferences,
        option = DietProfileOption.of(preferences),
        input = preferences.maxDailyCarbsAmount.toString(),
        customMode = preferences.dietMode,
    )

    private fun form(prototype: Prototype): DietPreferencesContentState.Form =
        assertIs<DietPreferencesContentState.Form>(factory.createFor(prototype).content)

    private fun grams(amount: Long) = UiText.Res(R.string.preferences_grams, UiArg.Numeric.Count(amount))
}
