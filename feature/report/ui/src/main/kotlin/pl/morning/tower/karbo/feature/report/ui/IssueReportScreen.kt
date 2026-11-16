package pl.morning.tower.karbo.feature.report.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.morning.tower.karbo.core.designsystem.component.ContentCrossfade
import pl.morning.tower.karbo.core.designsystem.component.FadeVisibility
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.component.Screen
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.event.ObserveEvents
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.report.api.model.IssueReason
import pl.morning.tower.karbo.feature.report.ui.camera.rememberPhotoPicker
import pl.morning.tower.karbo.feature.report.ui.component.FailedContent
import pl.morning.tower.karbo.feature.report.ui.component.IssueForm
import pl.morning.tower.karbo.feature.report.ui.component.PhotoPreview
import pl.morning.tower.karbo.feature.report.ui.component.SentContent
import pl.morning.tower.karbo.feature.report.ui.component.example
import pl.morning.tower.karbo.feature.report.ui.component.exampleForm
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnBackToReportClicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnPhotoPicked
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction.OnPreviewCancelled
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.OpenPhotoPicker
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.ReportNavigation
import pl.morning.tower.karbo.feature.report.ui.state.ExpectationState
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportContentState.Failed
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportContentState.Form
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportContentState.Sent
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportState
import pl.morning.tower.karbo.feature.report.ui.state.PhotoPreviewState
import pl.morning.tower.karbo.feature.report.ui.state.PhotosState
import pl.morning.tower.karbo.feature.report.ui.state.RatedAsState

@Composable
internal fun IssueReportScreen(onNavigation: (ReportNavigation) -> Unit) {
    val viewModel: IssueReportViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val photoPicker = rememberPhotoPicker { uri -> viewModel.onAction(OnPhotoPicked(uri)) }
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is ReportNavigation -> onNavigation(event)
            OpenPhotoPicker -> photoPicker.launch()
        }
    }
    BackHandler(enabled = state.content is Failed) {
        viewModel.onAction(OnBackToReportClicked)
    }
    BackHandler(enabled = state.photoPreview != null) {
        viewModel.onAction(OnPreviewCancelled)
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: IssueReportState, onAction: (IssueReportAction) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
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
        Ui.FadeVisibility(value = state.photoPreview) { preview ->
            PhotoPreview(preview, onAction)
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportEmptyPreview() {
    AppTheme {
        ScreenContent(IssueReportState(content = exampleForm(), photoPreview = null)) {}
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun IssueReportPhotosPreview() {
    AppTheme {
        ScreenContent(
            IssueReportState(
                content = exampleForm(
                    reason = IssueReason.NUTRITION,
                    details = "The label says 4 g carbs per 100 g.",
                    photos = PhotosState.example(count = 2),
                ),
                photoPreview = null,
            ),
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun IssueReportFullPhotosPreview() {
    AppTheme {
        ScreenContent(
            IssueReportState(
                content = exampleForm(
                    reason = IssueReason.PRODUCT,
                    photos = PhotosState.example(count = 3),
                ),
                photoPreview = null,
            ),
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportPhotoPreviewPreview() {
    AppTheme {
        ScreenContent(
            IssueReportState(
                content = exampleForm(reason = IssueReason.NUTRITION),
                photoPreview = PhotoPreviewState.example(),
            ),
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun IssueReportRatingPreview() {
    AppTheme {
        ScreenContent(
            IssueReportState(
                content = exampleForm(
                    reason = IssueReason.RATING,
                    expectation = ExpectationState.example(
                        ratedAs = RatedAsState.example(rate = DietRate.NOT_ADVISED, rating = Rating.Negative),
                        selected = DietRate.NEUTRAL,
                    ),
                ),
                photoPreview = null,
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
                content = exampleForm(
                    reason = IssueReason.NUTRITION,
                    details = "The label says 4 g carbs per 100 g.",
                ),
                photoPreview = null,
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
                content = exampleForm(
                    reason = IssueReason.NUTRITION,
                    details = "The label says 4 g carbs per 100 g.",
                    isSending = true,
                ),
                photoPreview = null,
            ),
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportSentPreview() {
    AppTheme {
        ScreenContent(IssueReportState(content = Sent, photoPreview = null)) {}
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportFailedPreview() {
    AppTheme {
        ScreenContent(IssueReportState(content = Failed, photoPreview = null)) {}
    }
}
