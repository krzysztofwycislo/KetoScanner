package pl.morning.tower.karbo.feature.report.ui.event

sealed interface ReportEvent {

    sealed interface ReportNavigation : ReportEvent {
        data object NavigateBack : ReportNavigation
        data object NavigateToHome : ReportNavigation
        data object NavigateToScan : ReportNavigation
    }
}
