package pl.handsome.club.feature.preferences.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
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
        val state = factory.createFor(Prototype(savedAmount = 40, input = "25"))

        assertEquals(
            DietPreferencesContentState.Form(
                summary = UiText.Res(R.string.preferences_summary, UiArg.Numeric.Count(40)),
                input = "25",
                inputError = null,
            ),
            state.content,
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
}
