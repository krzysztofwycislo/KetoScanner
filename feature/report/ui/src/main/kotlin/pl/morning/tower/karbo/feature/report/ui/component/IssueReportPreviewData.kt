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
import pl.morning.tower.karbo.feature.report.ui.state.MAX_REPORT_PHOTOS
import pl.morning.tower.karbo.feature.report.ui.state.PhotoPreviewState
import pl.morning.tower.karbo.feature.report.ui.state.PhotoThumbnailState
import pl.morning.tower.karbo.feature.report.ui.state.PhotosState
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

internal fun PhotoPreviewState.Companion.example() = PhotoPreviewState(
    imageUri = "",
    counter = resources.photoCounter(number = 2, total = MAX_REPORT_PHOTOS),
    title = resources.previewTitle(IssueReason.NUTRITION),
    body = resources.previewBody(IssueReason.NUTRITION),
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
