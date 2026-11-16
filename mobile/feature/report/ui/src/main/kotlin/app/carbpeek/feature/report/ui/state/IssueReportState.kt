package app.carbpeek.feature.report.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.report.api.model.IssueReason
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureState
import kotlinx.collections.immutable.PersistentList

@Immutable
internal data class IssueReportState(
    val content: IssueReportContentState,
    val photoCapture: PhotoCaptureState?,
)

@Immutable
internal sealed interface IssueReportContentState {

    data class Form(
        val product: ProductSummaryState,
        val reasons: PersistentList<ReasonChipState>,
        val details: String,
        val detailsPlaceholder: UiText,
        val expectation: ExpectationState?,
        val photos: PhotosState?,
        val isEditable: Boolean,
        val sendButton: SendButtonState,
    ) : IssueReportContentState

    data object Sent : IssueReportContentState

    data object Failed : IssueReportContentState
}

@Immutable
internal data class ProductSummaryState(
    val icon: AppIcon,
    val name: UiText,
    val subtitle: UiText?,
) {
    companion object
}

@Immutable
internal data class ReasonChipState(
    val reason: IssueReason,
    val label: UiText,
    val isSelected: Boolean,
) {
    companion object
}

@Immutable
internal data class ExpectationState(
    val ratedAs: RatedAsState?,
    val options: PersistentList<ExpectedRateOptionState>,
) {
    companion object
}

@Immutable
internal data class RatedAsState(
    val label: UiText,
    val rating: Rating,
) {
    companion object
}

@Immutable
internal data class ExpectedRateOptionState(
    val rate: DietRate,
    val label: UiText,
    val rating: Rating,
    val isSelected: Boolean,
) {
    companion object
}

@Immutable
internal data class PhotosState(
    val items: PersistentList<PhotoThumbnailState>,
    val showAddTile: Boolean,
) {
    companion object
}

@Immutable
internal data class PhotoThumbnailState(
    val uri: String,
    val contentDescription: UiText,
    val removeLabel: UiText,
) {
    companion object
}
