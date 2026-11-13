package pl.morning.tower.karbo.feature.report.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.ReportNavigation

@Serializable
data class ReportRoute(val barcode: String)

fun NavGraphBuilder.reportDestination(onNavigation: (ReportNavigation) -> Unit) {
    composable<ReportRoute> {
        IssueReportScreen(onNavigation)
    }
}
