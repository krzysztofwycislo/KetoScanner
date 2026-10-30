package pl.handsome.club.feature.preferences.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.preferences.ui.state.DietProfileOption

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

    fun ratingSubtitle(dailyCarbs: Int): UiText =
        UiText.Res(R.string.preferences_rating_subtitle, UiArg.Numeric.Count(dailyCarbs.toLong()))

    fun ratingLabel(rate: DietRate, mode: DietMode): UiText = when (rate) {
        DietRate.GOOD -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.preferences_rating_good_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.preferences_rating_good_low_carb)
        }

        DietRate.NEUTRAL -> UiText.Res(R.string.preferences_rating_neutral)

        DietRate.NOT_ADVISED -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.preferences_rating_not_advised_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.preferences_rating_not_advised_low_carb)
        }
    }

    fun upTo(grams: Double): UiText = UiText.Res(R.string.preferences_rating_up_to, UiArg.Numeric.Grams(grams))

    fun over(grams: Double): UiText = UiText.Res(R.string.preferences_rating_over, UiArg.Numeric.Grams(grams))

    fun ratingFootnote(mode: DietMode): UiText = when (mode) {
        DietMode.KETO -> UiText.Res(R.string.preferences_rating_footnote_keto)
        DietMode.LOW_CARB -> UiText.Res(R.string.preferences_rating_footnote_low_carb)
    }

    fun saved(): UiText = UiText.Res(R.string.preferences_saved)

    fun saveFailed(error: AppError): UiText = error.toUiText()

    fun loadError(error: AppError): UiText = error.toUiText()
}
