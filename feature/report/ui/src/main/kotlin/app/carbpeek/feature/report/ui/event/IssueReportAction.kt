package app.carbpeek.feature.report.ui.event

import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.report.api.model.IssueReason
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction

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
    data class OnRemovePhotoClicked(val uri: String) : IssueReportAction
    data class OnCapture(val action: PhotoCaptureAction) : IssueReportAction
}
