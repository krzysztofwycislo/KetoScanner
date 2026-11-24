package app.carbpeek.feature.preferences.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.feature.preferences.ui.state.DietProfileOption

internal class DietPreferencesResources {

    fun optionTitle(option: DietProfileOption): UiText = when (option) {
        DietProfileOption.STRICT_KETO -> UiText.Res(R.string.preferences_preset_strict_keto)
        DietProfileOption.KETO -> UiText.Res(R.string.preferences_preset_keto)
        DietProfileOption.LOW_CARB -> UiText.Res(R.string.preferences_preset_low_carb)
    }

    fun optionNote(option: DietProfileOption): UiText = when (option) {
        DietProfileOption.STRICT_KETO -> UiText.Res(R.string.preferences_preset_strict_keto_note)
        DietProfileOption.KETO -> UiText.Res(R.string.preferences_preset_keto_note)
        DietProfileOption.LOW_CARB -> UiText.Res(R.string.preferences_preset_low_carb_note)
    }

    fun grams(amount: Int): UiText = UiText.Res(R.string.preferences_grams, UiArg.Numeric.Count(amount.toLong()))

    fun saved(): UiText = UiText.Res(R.string.preferences_saved)

    fun saveFailed(error: AppError): UiText = error.toUiText()

    fun loadError(error: AppError): UiText = error.toUiText()
}
