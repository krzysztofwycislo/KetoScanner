package pl.morning.tower.karbo.feature.home.ui

import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.designsystem.text.UiArg
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.designsystem.text.toUiText
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset
import kotlin.time.Instant

internal class HomeResources {

    fun profileLabel(preferences: DietPreferences?): UiText {
        if (preferences == null) return UiText.Res(R.string.home_profile_default)
        return when (DietPreset.of(preferences)) {
            DietPreset.STRICT_KETO -> UiText.Res(R.string.home_preset_strict_keto)
            DietPreset.KETO -> UiText.Res(R.string.home_preset_keto)
            DietPreset.LOW_CARB -> UiText.Res(R.string.home_preset_low_carb)
            null -> UiText.Res(
                R.string.home_profile_label,
                UiArg.Nested(modeLabel(preferences.dietMode)),
                UiArg.Numeric.Count(preferences.maxDailyCarbsAmount.toLong()),
            )
        }
    }

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.home_unnamed_product) else UiText.Raw(name)

    fun today(instant: Instant): UiText = UiText.Res(R.string.home_time_today, UiArg.Time(instant))

    fun yesterday(): UiText = UiText.Res(R.string.home_time_yesterday)

    fun date(instant: Instant): UiText = UiText.Res(R.string.home_time_date, UiArg.Date(instant))

    fun carbs(grams: Double?): UiText =
        if (grams == null) UiText.Res(R.string.home_carbs_missing)
        else UiText.Res(R.string.home_carbs, UiArg.Numeric.Grams(grams))

    fun verdictDescription(rate: DietRate?, mode: DietMode, carbs: Double?): UiText {
        val verdict = verdict(rate, mode)
        return if (carbs == null) verdict
        else UiText.Res(
            R.string.home_verdict_with_carbs,
            UiArg.Nested(verdict),
            UiArg.Nested(UiText.Res(R.string.home_carbs_per_100g, UiArg.Numeric.Grams(carbs))),
        )
    }

    fun loadError(error: AppError): UiText = error.toUiText()

    private fun modeLabel(mode: DietMode): UiText = when (mode) {
        DietMode.KETO -> UiText.Res(R.string.home_mode_keto)
        DietMode.LOW_CARB -> UiText.Res(R.string.home_mode_low_carb)
    }

    private fun verdict(rate: DietRate?, mode: DietMode): UiText = when (rate) {
        DietRate.GOOD -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.home_verdict_good_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.home_verdict_good_low_carb)
        }
        DietRate.NEUTRAL -> UiText.Res(R.string.home_verdict_neutral)
        DietRate.NOT_ADVISED -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.home_verdict_not_advised_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.home_verdict_not_advised_low_carb)
        }
        null -> UiText.Res(R.string.home_verdict_unknown)
    }
}
