package pl.morning.tower.karbo.feature.report.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.morning.tower.karbo.core.designsystem.component.ContentCrossfade
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.component.Screen
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.event.ObserveEvents
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.report.api.model.IssueReason
import pl.morning.tower.karbo.feature.report.ui.component.FailedContent
import pl.morning.tower.karbo.feature.report.ui.component.IssueForm
import pl.morning.tower.karbo.feature.report.ui.component.SentContent
import pl.morning.tower.karbo.feature.report.ui.component.example
import pl.morning.tower.karbo.feature.report.ui.component.exampleForm
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnBackToReportClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.ReportNavigation
import pl.morning.tower.karbo.feature.report.ui.state.ExpectationState
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportContentState.Failed
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportContentState.Form
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportContentState.Sent
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportState
import pl.morning.tower.karbo.feature.report.ui.state.RatedAsState

@Composable
internal fun IssueReportScreen(onNavigation: (ReportNavigation) -> Unit) {
    val viewModel: IssueReportViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is ReportNavigation -> onNavigation(event)
        }
    }
    BackHandler(enabled = state.content is Failed) {
        viewModel.onAction(OnBackToReportClicked)
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: IssueReportState, onAction: (IssueReportAction) -> Unit) {
    Ui.Screen { innerPadding ->
        Ui.ContentCrossfade(
            targetState = state.content,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) { content ->
            when (content) {
                is Form -> IssueForm(content, onAction)
                Sent -> SentContent(onAction)
                Failed -> FailedContent(onAction)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportEmptyPreview() {
    AppTheme {
        ScreenContent(IssueReportState(exampleForm())) {}
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun IssueReportRatingPreview() {
    AppTheme {
        ScreenContent(
            IssueReportState(
                exampleForm(
                    reason = IssueReason.RATING,
                    expectation = ExpectationState.example(
                        ratedAs = RatedAsState.example(rate = DietRate.NOT_ADVISED, rating = Rating.Negative),
                        selected = DietRate.NEUTRAL,
                    ),
                ),
            ),
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportNutritionPreview() {
    AppTheme {
        ScreenContent(
            IssueReportState(
                exampleForm(
                    reason = IssueReason.NUTRITION,
                    details = "The label says 4 g carbs per 100 g.",
                ),
            ),
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportSendingPreview() {
    AppTheme {
        ScreenContent(
            IssueReportState(
                exampleForm(
                    reason = IssueReason.NUTRITION,
                    details = "The label says 4 g carbs per 100 g.",
                    isSending = true,
                ),
            ),
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportSentPreview() {
    AppTheme {
        ScreenContent(IssueReportState(Sent)) {}
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportFailedPreview() {
    AppTheme {
        ScreenContent(IssueReportState(Failed)) {}
    }
}
