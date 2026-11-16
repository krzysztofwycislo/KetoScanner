package pl.morning.tower.karbo.feature.report.ui.event

sealed interface IssueReportEvent {

    data object OpenPhotoPicker : IssueReportEvent

    sealed interface ReportNavigation : IssueReportEvent {
        data object NavigateBack : ReportNavigation
        data object NavigateToHome : ReportNavigation
        data object NavigateToScan : ReportNavigation
    }
}
