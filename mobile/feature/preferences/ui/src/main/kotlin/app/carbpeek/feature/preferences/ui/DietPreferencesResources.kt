package app.carbpeek.feature.preferences.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.ui.state.DietProfileOption

internal class DietPreferencesResources {

    fun optionTitle(option: DietProfileOption): UiText = when (option) {
        DietProfileOption.STRICT_KETO -> UiText.Res(R.string.preferences_preset_strict_keto)
        DietProfileOption.KETO -> UiText.Res(R.string.preferences_preset_keto)
        DietProfileOption.LOW_CARB -> UiText.Res(R.string.preferences_preset_low_carb)
        DietProfileOption.CUSTOM -> UiText.Res(R.string.preferences_preset_custom)
    }

    fun optionNote(option: DietProfileOption): UiText = when (option) {
        DietProfileOption.STRICT_KETO -> UiText.Res(R.string.preferences_preset_strict_keto_note)
        DietProfileOption.KETO -> UiText.Res(R.string.preferences_preset_keto_note)
        DietProfileOption.LOW_CARB -> UiText.Res(R.string.preferences_preset_low_carb_note)
        DietProfileOption.CUSTOM -> UiText.Res(R.string.preferences_preset_custom_note)
    }

    fun grams(amount: Int): UiText = UiText.Res(R.string.preferences_grams, UiArg.Numeric.Count(amount.toLong()))

    fun inputError(): UiText = UiText.Res(
        R.string.preferences_input_error,
        UiArg.Numeric.Count(DietPreferences.MAX_DAILY_CARBS_RANGE.first.toLong()),
        UiArg.Numeric.Count(DietPreferences.MAX_DAILY_CARBS_RANGE.last.toLong()),
    )

    fun dietModeLabel(mode: DietMode): UiText = when (mode) {
        DietMode.KETO -> UiText.Res(R.string.preferences_diet_mode_keto)
        DietMode.LOW_CARB -> UiText.Res(R.string.preferences_diet_mode_low_carb)
    }

    fun saved(): UiText = UiText.Res(R.string.preferences_saved)

    fun saveFailed(error: AppError): UiText = error.toUiText()

    fun loadError(error: AppError): UiText = error.toUiText()
}
