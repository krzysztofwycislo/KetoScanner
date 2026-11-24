package app.carbpeek.feature.home.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import kotlin.time.Instant

internal class HomeResources {

    fun profileLabel(preferences: DietPreferences?): UiText {
        if (preferences == null) return UiText.Res(R.string.home_profile_default)
        return when (DietPreset.of(preferences)) {
            DietPreset.STRICT_KETO -> UiText.Res(R.string.home_preset_strict_keto)
            DietPreset.KETO -> UiText.Res(R.string.home_preset_keto)
            DietPreset.LOW_CARB -> UiText.Res(R.string.home_preset_low_carb)
            null -> UiText.Res(R.string.home_preset_no_diet)
        }
    }

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.home_unnamed_product) else UiText.Raw(name)

    fun today(instant: Instant): UiText = UiText.Res(R.string.home_time_today, UiArg.Time(instant))

    fun yesterday(): UiText = UiText.Res(R.string.home_time_yesterday)

    fun date(instant: Instant): UiText = UiText.Res(R.string.home_time_date, UiArg.Date(instant))

    fun carbs(grams: Double?, mode: DietMode?): UiText = when {
        grams == null -> UiText.Res(R.string.home_carbs_missing)
        mode == null -> UiText.Res(R.string.home_carbs_named, UiArg.Numeric.Grams(grams))
        else -> UiText.Res(R.string.home_carbs, UiArg.Numeric.Grams(grams))
    }

    fun verdictDescription(rate: DietRate?, mode: DietMode?, carbs: Double?): UiText {
        val verdict = mode?.let { verdict(rate = rate, mode = it) }
        val carbsPer100g = carbs?.let { UiText.Res(R.string.home_carbs_per_100g, UiArg.Numeric.Grams(it)) }
        return when {
            carbsPer100g == null -> verdict ?: UiText.Res(R.string.home_verdict_unknown)
            verdict == null -> carbsPer100g
            else -> UiText.Res(
                R.string.home_verdict_with_carbs,
                UiArg.Nested(verdict),
                UiArg.Nested(carbsPer100g),
            )
        }
    }

    fun loadError(error: AppError): UiText = error.toUiText()

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
