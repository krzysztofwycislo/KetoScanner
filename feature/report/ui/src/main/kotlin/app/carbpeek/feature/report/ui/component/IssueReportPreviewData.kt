package app.carbpeek.feature.report.ui.component

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.report.api.model.IssueReason
import app.carbpeek.feature.report.ui.IssueReportResources
import app.carbpeek.feature.report.ui.state.ExpectationState
import app.carbpeek.feature.report.ui.state.ExpectedRateOptionState
import app.carbpeek.feature.report.ui.state.IssueReportContentState.Form
import app.carbpeek.feature.report.ui.state.MAX_REPORT_PHOTOS
import app.carbpeek.feature.report.ui.state.PhotoThumbnailState
import app.carbpeek.feature.report.ui.state.PhotosState
import app.carbpeek.feature.report.ui.state.ProductSummaryState
import app.carbpeek.feature.report.ui.state.RatedAsState
import app.carbpeek.feature.report.ui.state.ReasonChipState
import app.carbpeek.feature.report.ui.state.SendButtonState
import kotlinx.collections.immutable.toPersistentList

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

internal fun PhotosState.Companion.example(count: Int) = PhotosState(
    items = (1..count).map { number ->
        PhotoThumbnailState(
            uri = "",
            contentDescription = resources.photoAttached(number),
            removeLabel = resources.removePhoto(number),
        )
    }.toPersistentList(),
    showAddTile = count < MAX_REPORT_PHOTOS,
)

internal fun exampleForm(
    reason: IssueReason? = null,
    details: String = "",
    expectation: ExpectationState? = null,
    photos: PhotosState? = PhotosState.example(count = 0).takeIf { reason != IssueReason.RATING },
    isSending: Boolean = false,
) = Form(
    product = ProductSummaryState.example(),
    reasons = ReasonChipState.examples(reason),
    details = details,
    detailsPlaceholder = resources.detailsPlaceholder(reason),
    expectation = expectation,
    photos = photos,
    isEditable = !isSending,
    sendButton = SendButtonState.example(isReady = reason != null, isLoading = isSending),
)

private fun DietRate.rating(): Rating = when (this) {
    DietRate.GOOD -> Rating.Positive
    DietRate.NEUTRAL -> Rating.Neutral
    DietRate.NOT_ADVISED -> Rating.Negative
}
