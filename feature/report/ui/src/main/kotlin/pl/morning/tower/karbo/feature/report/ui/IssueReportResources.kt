package pl.morning.tower.karbo.feature.report.ui

import pl.morning.tower.karbo.core.designsystem.text.UiArg
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.report.api.model.IssueReason

internal class IssueReportResources {

    fun reasonLabel(reason: IssueReason): UiText = UiText.Res(
        when (reason) {
            IssueReason.RATING -> R.string.report_reason_rating
            IssueReason.NUTRITION -> R.string.report_reason_nutrition
            IssueReason.PRODUCT -> R.string.report_reason_product
            IssueReason.OTHER -> R.string.report_reason_other
        },
    )

    fun detailsPlaceholder(reason: IssueReason?): UiText = UiText.Res(
        when (reason) {
            IssueReason.RATING -> R.string.report_placeholder_rating
            IssueReason.NUTRITION -> R.string.report_placeholder_nutrition
            IssueReason.PRODUCT -> R.string.report_placeholder_product
            IssueReason.OTHER, null -> R.string.report_placeholder_other
        },
    )

    fun verdictLabel(rate: DietRate, mode: DietMode): UiText {
        val id = when (rate) {
            DietRate.GOOD -> when (mode) {
                DietMode.KETO -> R.string.report_verdict_good_keto
                DietMode.LOW_CARB -> R.string.report_verdict_good_low_carb
            }
            DietRate.NEUTRAL -> R.string.report_verdict_neutral
            DietRate.NOT_ADVISED -> when (mode) {
                DietMode.KETO -> R.string.report_verdict_not_advised_keto
                DietMode.LOW_CARB -> R.string.report_verdict_not_advised_low_carb
            }
        }
        return UiText.Res(id)
    }

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.report_unnamed_product) else UiText.Raw(name)

    fun barcode(barcode: String): UiText = UiText.Raw(barcode)

    fun productSubtitle(brand: String, barcode: String): UiText =
        UiText.Res(R.string.report_product_subtitle, UiArg.Text(brand), UiArg.Text(barcode))

    fun sendLabel(): UiText = UiText.Res(R.string.report_send)

    fun pickReasonLabel(): UiText = UiText.Res(R.string.report_pick_reason)
}
