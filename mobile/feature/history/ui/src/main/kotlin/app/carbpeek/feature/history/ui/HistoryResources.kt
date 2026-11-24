package app.carbpeek.feature.history.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.history.ui.state.HistoryFilter
import app.carbpeek.feature.preferences.api.model.DietMode
import kotlin.time.Instant

internal class HistoryResources {

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.history_unnamed_product) else UiText.Raw(name)

    fun filterLabel(filter: HistoryFilter, mode: DietMode): UiText = when (filter) {
        HistoryFilter.ALL -> UiText.Res(R.string.history_filter_all)
        HistoryFilter.GOOD -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.history_filter_good_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.history_filter_good_low_carb)
        }
        HistoryFilter.NEUTRAL -> UiText.Res(R.string.history_filter_neutral)
        HistoryFilter.NOT_ADVISED -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.history_filter_not_advised_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.history_filter_not_advised_low_carb)
        }
    }

    fun today(): UiText = UiText.Res(R.string.history_group_today)

    fun yesterday(): UiText = UiText.Res(R.string.history_group_yesterday)

    fun thisWeek(): UiText = UiText.Res(R.string.history_group_this_week)

    fun dateGroup(instant: Instant): UiText = UiText.Res(R.string.history_group_date, UiArg.Date(instant))

    fun time(instant: Instant): UiText = UiText.Res(R.string.history_row_time, UiArg.Time(instant))

    fun weekday(instant: Instant): UiText = UiText.Res(R.string.history_row_time, UiArg.Weekday(instant))

    fun carbs(grams: Double?, mode: DietMode?): UiText = when {
        grams == null -> UiText.Res(R.string.history_carbs_missing)
        mode == null -> UiText.Res(R.string.history_carbs_named, UiArg.Numeric.Grams(grams))
        else -> UiText.Res(R.string.history_carbs, UiArg.Numeric.Grams(grams))
    }

    fun verdictDescription(rate: DietRate?, mode: DietMode?, carbs: Double?): UiText {
        val verdict = mode?.let { verdict(rate = rate, mode = it) }
        val carbsPer100g = carbs?.let { UiText.Res(R.string.history_carbs_per_100g, UiArg.Numeric.Grams(it)) }
        return when {
            carbsPer100g == null -> verdict ?: UiText.Res(R.string.history_verdict_unknown)
            verdict == null -> carbsPer100g
            else -> UiText.Res(
                R.string.history_verdict_with_carbs,
                UiArg.Nested(verdict),
                UiArg.Nested(carbsPer100g),
            )
        }
    }

    fun noResults(mode: DietMode?): UiText =
        if (mode == null) {
            UiText.Res(R.string.history_no_results_no_filters)
        } else {
            UiText.Res(R.string.history_no_results)
        }

    fun loadError(error: AppError): UiText = error.toUiText()

    private fun verdict(rate: DietRate?, mode: DietMode): UiText = when (rate) {
        DietRate.GOOD -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.history_verdict_good_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.history_verdict_good_low_carb)
        }
        DietRate.NEUTRAL -> UiText.Res(R.string.history_verdict_neutral)
        DietRate.NOT_ADVISED -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.history_verdict_not_advised_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.history_verdict_not_advised_low_carb)
        }
        null -> UiText.Res(R.string.history_verdict_unknown)
    }
}
