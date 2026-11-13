package pl.morning.tower.karbo.feature.report.ui.component

import kotlinx.collections.immutable.toPersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.report.api.model.IssueReason
import pl.morning.tower.karbo.feature.report.ui.IssueReportResources
import pl.morning.tower.karbo.feature.report.ui.state.ExpectationState
import pl.morning.tower.karbo.feature.report.ui.state.ExpectedRateOptionState
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportContentState.Form
import pl.morning.tower.karbo.feature.report.ui.state.ProductSummaryState
import pl.morning.tower.karbo.feature.report.ui.state.RatedAsState
import pl.morning.tower.karbo.feature.report.ui.state.ReasonChipState
import pl.morning.tower.karbo.feature.report.ui.state.SendButtonState

private val resources = IssueReportResources()

internal fun ProductSummaryState.Companion.example() = ProductSummaryState(
    icon = AppIcons.Can,
    name = UiText.Raw("Coca-Cola Original Taste"),
    subtitle = resources.productSubtitle(brand = "Coca-Cola", barcode = "5449000000996"),
)

internal fun ReasonChipState.Companion.examples(selected: IssueReason?) = IssueReason.entries.map { reason ->
    ReasonChipState(
        reason = reason,
        label = resources.reasonLabel(reason),
        isSelected = reason == selected,
    )
}.toPersistentList()

internal fun RatedAsState.Companion.example(rate: DietRate, rating: Rating) = RatedAsState(
    label = resources.verdictLabel(rate = rate, mode = DietMode.KETO),
    rating = rating,
)

internal fun ExpectationState.Companion.example(ratedAs: RatedAsState?, selected: DietRate?) = ExpectationState(
    ratedAs = ratedAs,
    options = DietRate.entries.map { rate ->
        ExpectedRateOptionState(
            rate = rate,
            label = resources.verdictLabel(rate = rate, mode = DietMode.KETO),
            rating = rate.rating(),
            isSelected = rate == selected,
        )
    }.toPersistentList(),
)

internal fun SendButtonState.Companion.example(isReady: Boolean, isLoading: Boolean = false) = SendButtonState(
    label = if (isReady) resources.sendLabel() else resources.pickReasonLabel(),
    isEnabled = isReady,
    isLoading = isLoading,
)

internal fun exampleForm(
    reason: IssueReason? = null,
    details: String = "",
    expectation: ExpectationState? = null,
    isSending: Boolean = false,
) = Form(
    product = ProductSummaryState.example(),
    reasons = ReasonChipState.examples(reason),
    details = details,
    detailsPlaceholder = resources.detailsPlaceholder(reason),
    expectation = expectation,
    isEditable = !isSending,
    sendButton = SendButtonState.example(isReady = reason != null, isLoading = isSending),
)

private fun DietRate.rating(): Rating = when (this) {
    DietRate.GOOD -> Rating.Positive
    DietRate.NEUTRAL -> Rating.Neutral
    DietRate.NOT_ADVISED -> Rating.Negative
}
