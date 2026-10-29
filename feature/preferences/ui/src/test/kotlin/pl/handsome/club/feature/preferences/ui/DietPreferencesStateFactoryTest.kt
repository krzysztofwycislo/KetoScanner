package pl.handsome.club.feature.preferences.ui

import kotlinx.collections.immutable.persistentListOf
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.ui.state.DietModeOptionState
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStateFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import pl.handsome.club.core.designsystem.R as DesignR
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStatePrototype as Prototype

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
    fun `when load error is set then content is error even if an amount was loaded`() {
        val state = factory.createFor(Prototype(savedAmount = 40, input = "40", loadError = AppError.Storage))

        assertEquals(
            DietPreferencesContentState.Error(UiText.Res(DesignR.string.designsystem_error_storage)),
            state.content,
        )
    }

    @Test
    fun `when amount is loaded then content is form with summary and input`() {
        val state = factory.createFor(Prototype(savedAmount = 40, savedMode = DietMode.KETO, input = "25"))

        assertEquals(
            DietPreferencesContentState.Form(
                dietModes = persistentListOf(
                    DietModeOptionState(
                        mode = DietMode.KETO,
                        label = UiText.Res(R.string.preferences_diet_mode_keto),
                        description = UiText.Res(R.string.preferences_diet_mode_keto_description),
                        isSelected = true,
                    ),
                    DietModeOptionState(
                        mode = DietMode.LOW_CARB,
                        label = UiText.Res(R.string.preferences_diet_mode_low_carb),
                        description = UiText.Res(R.string.preferences_diet_mode_low_carb_description),
                        isSelected = false,
                    ),
                ),
                summary = UiText.Res(R.string.preferences_summary, UiArg.Numeric.Count(40)),
                input = "25",
                inputError = null,
                isSaving = false,
            ),
            state.content,
        )
    }

    @Test
    fun `when low carb is saved then only the low carb option is selected`() {
        val state = factory.createFor(Prototype(savedAmount = 40, savedMode = DietMode.LOW_CARB, input = "40"))

        val form = state.content as DietPreferencesContentState.Form
        assertEquals(
            listOf(DietMode.KETO to false, DietMode.LOW_CARB to true),
            form.dietModes.map { it.mode to it.isSelected },
        )
    }

    @Test
    fun `when input is invalid then form shows the range error`() {
        val state = factory.createFor(Prototype(savedAmount = 40, input = "0", isInputInvalid = true))

        val form = state.content as DietPreferencesContentState.Form
        assertEquals(
            UiText.Res(R.string.preferences_input_error, UiArg.Numeric.Count(1), UiArg.Numeric.Count(999)),
            form.inputError,
        )
    }

    @Test
    fun `when saving is in progress then form shows it`() {
        val state = factory.createFor(Prototype(savedAmount = 40, input = "40", isSaving = true))

        assertEquals(true, (state.content as DietPreferencesContentState.Form).isSaving)
    }
}
