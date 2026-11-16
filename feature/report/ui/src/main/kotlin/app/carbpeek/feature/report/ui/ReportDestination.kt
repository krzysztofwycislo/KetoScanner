package app.carbpeek.feature.report.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation
import kotlinx.serialization.Serializable

@Serializable
enum class ReportKind {
    ISSUE,
    MISSING_PRODUCT,
}

@Serializable
data class ReportRoute(val barcode: String, val kind: ReportKind)

fun NavGraphBuilder.reportDestination(onNavigation: (ReportNavigation) -> Unit) {
    composable<ReportRoute> { entry ->
        when (entry.toRoute<ReportRoute>().kind) {
            ReportKind.ISSUE -> IssueReportScreen(onNavigation)
            ReportKind.MISSING_PRODUCT -> MissingProductScreen(onNavigation)
        }
    }
}
