package pl.morning.tower.karbo.feature.report.ui.event

import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.report.api.model.IssueReason

internal sealed interface IssueReportAction {
    data object OnCloseClicked : IssueReportAction
    data class OnReasonClicked(val reason: IssueReason) : IssueReportAction
    data class OnDetailsChanged(val details: String) : IssueReportAction
    data class OnExpectedRateClicked(val rate: DietRate) : IssueReportAction
    data object OnSendClicked : IssueReportAction
    data object OnTryAgainClicked : IssueReportAction
    data object OnBackToReportClicked : IssueReportAction
    data object OnHomeClicked : IssueReportAction
    data object OnScanNextClicked : IssueReportAction
    data object OnAddPhotoClicked : IssueReportAction
    data class OnPhotoPicked(val uri: String) : IssueReportAction
    data class OnRemovePhotoClicked(val uri: String) : IssueReportAction
    data object OnRetakeClicked : IssueReportAction
    data object OnUsePhotoClicked : IssueReportAction
    data object OnPreviewCancelled : IssueReportAction
}
