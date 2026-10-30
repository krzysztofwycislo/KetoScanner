package pl.handsome.club.feature.history.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.history.ui.state.HistoryFilter
import pl.handsome.club.feature.preferences.api.model.DietMode
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

    fun carbs(grams: Double?): UiText =
        if (grams == null) UiText.Res(R.string.history_carbs_missing)
        else UiText.Res(R.string.history_carbs, UiArg.Numeric.Grams(grams))

    fun verdictDescription(rate: DietRate?, mode: DietMode, carbs: Double?): UiText {
        val verdict = verdict(rate, mode)
        return if (carbs == null) verdict
        else UiText.Res(
            R.string.history_verdict_with_carbs,
            UiArg.Nested(verdict),
            UiArg.Nested(UiText.Res(R.string.history_carbs_per_100g, UiArg.Numeric.Grams(carbs))),
        )
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
