package pl.handsome.club.feature.preferences.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences

internal class DietPreferencesResources {

    fun summary(amount: Int): UiText =
        UiText.Res(R.string.preferences_summary, UiArg.Numeric.Count(amount.toLong()))

    fun inputError(): UiText = UiText.Res(
        R.string.preferences_input_error,
        UiArg.Numeric.Count(DietPreferences.MAX_DAILY_CARBS_RANGE.first.toLong()),
        UiArg.Numeric.Count(DietPreferences.MAX_DAILY_CARBS_RANGE.last.toLong()),
    )

    fun dietModeLabel(mode: DietMode): UiText = when (mode) {
        DietMode.KETO -> UiText.Res(R.string.preferences_diet_mode_keto)
        DietMode.LOW_CARB -> UiText.Res(R.string.preferences_diet_mode_low_carb)
    }

    fun dietModeDescription(mode: DietMode): UiText = when (mode) {
        DietMode.KETO -> UiText.Res(R.string.preferences_diet_mode_keto_description)
        DietMode.LOW_CARB -> UiText.Res(R.string.preferences_diet_mode_low_carb_description)
    }

    fun saved(): UiText = UiText.Res(R.string.preferences_saved)

    fun saveFailed(error: AppError): UiText = error.toUiText()

    fun loadError(error: AppError): UiText = error.toUiText()
}
