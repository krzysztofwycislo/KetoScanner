package pl.morning.tower.karbo.feature.report.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.report.api.model.IssueReason

@Immutable
internal data class IssueReportState(
    val content: IssueReportContentState,
)

@Immutable
internal sealed interface IssueReportContentState {

    data class Form(
        val product: ProductSummaryState,
        val reasons: PersistentList<ReasonChipState>,
        val details: String,
        val detailsPlaceholder: UiText,
        val expectation: ExpectationState?,
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
internal data class SendButtonState(
    val label: UiText,
    val isEnabled: Boolean,
    val isLoading: Boolean,
) {
    companion object
}
