package app.carbpeek.feature.report.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.carbpeek.core.designsystem.component.ContentCrossfade
import app.carbpeek.core.designsystem.component.FadeVisibility
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.report.api.model.IssueReason
import app.carbpeek.feature.report.ui.camera.PhotoCaptureEffects
import app.carbpeek.feature.report.ui.capture.component.PhotoCaptureOverlay
import app.carbpeek.feature.report.ui.capture.component.example
import app.carbpeek.feature.report.ui.capture.state.CaptureStepState
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureState
import app.carbpeek.feature.report.ui.component.FailedContent
import app.carbpeek.feature.report.ui.component.IssueForm
import app.carbpeek.feature.report.ui.component.SentContent
import app.carbpeek.feature.report.ui.component.example
import app.carbpeek.feature.report.ui.component.exampleForm
import app.carbpeek.feature.report.ui.event.IssueReportAction
import app.carbpeek.feature.report.ui.event.IssueReportAction.OnBackToReportClicked
import app.carbpeek.feature.report.ui.event.IssueReportAction.OnCapture
import app.carbpeek.feature.report.ui.event.IssueReportAction.OnHomeClicked
import app.carbpeek.feature.report.ui.event.IssueReportAction.OnScanNextClicked
import app.carbpeek.feature.report.ui.event.IssueReportAction.OnTryAgainClicked
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation
import app.carbpeek.feature.report.ui.state.ExpectationState
import app.carbpeek.feature.report.ui.state.IssueReportContentState
import app.carbpeek.feature.report.ui.state.IssueReportContentState.Failed
import app.carbpeek.feature.report.ui.state.IssueReportContentState.Form
import app.carbpeek.feature.report.ui.state.IssueReportContentState.Sent
import app.carbpeek.feature.report.ui.state.IssueReportState
import app.carbpeek.feature.report.ui.state.PhotosState
import app.carbpeek.feature.report.ui.state.RatedAsState
import app.carbpeek.photocamera.PhotoCameraState
import app.carbpeek.photocamera.rememberPhotoCameraState
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun IssueReportScreen(onNavigation: (ReportNavigation) -> Unit) {
    val viewModel: IssueReportViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val camera = rememberPhotoCameraState()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is ReportNavigation -> onNavigation(event)
        }
    }
    PhotoCaptureEffects(
        isOpen = state.photoCapture != null,
        events = viewModel.captureEvents,
        camera = camera,
        onAction = { viewModel.onAction(OnCapture(it)) },
    )
    BackHandler(enabled = state.content is Failed) {
        viewModel.onAction(OnBackToReportClicked)
    }

    ScreenContent(
        state = state,
        camera = camera,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun ScreenContent(
    state: IssueReportState,
    camera: PhotoCameraState,
    onAction: (IssueReportAction) -> Unit,
) {
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
                    Sent -> {
                        SentContent(
                            title = stringResource(R.string.report_sent_title),
                            body = stringResource(R.string.report_sent_body),
                            steps = persistentListOf(
                                stringResource(R.string.report_step_sent),
                                stringResource(R.string.report_step_check),
                                stringResource(R.string.report_step_fix),
                            ),
                            onHomeClicked = { onAction(OnHomeClicked) },
                            onScanNextClicked = { onAction(OnScanNextClicked) },
                        )
                    }
                    Failed -> {
                        FailedContent(
                            onTryAgainClicked = { onAction(OnTryAgainClicked) },
                            onBackToReportClicked = { onAction(OnBackToReportClicked) },
                        )
                    }
                }
            }
        }
        Ui.FadeVisibility(value = state.photoCapture) { capture ->
            PhotoCaptureOverlay(
                state = capture,
                camera = camera,
                onAction = { action -> onAction(OnCapture(action)) },
            )
        }
    }
}

@Composable
private fun ReportPreview(content: IssueReportContentState, photoCapture: PhotoCaptureState? = null) {
    AppTheme {
        ScreenContent(
            state = IssueReportState(content = content, photoCapture = photoCapture),
            camera = rememberPhotoCameraState(),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportEmptyPreview() {
    ReportPreview(content = exampleForm())
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun IssueReportPhotosPreview() {
    ReportPreview(
        content = exampleForm(
            reason = IssueReason.NUTRITION,
            details = "The label says 4 g carbs per 100 g.",
            photos = PhotosState.example(count = 2),
        ),
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun IssueReportFullPhotosPreview() {
    ReportPreview(
        content = exampleForm(
            reason = IssueReason.PRODUCT,
            photos = PhotosState.example(count = 3),
        ),
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportPhotoCapturePreview() {
    ReportPreview(
        content = exampleForm(reason = IssueReason.NUTRITION),
        photoCapture = PhotoCaptureState.example(CaptureStepState.Camera.example()),
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun IssueReportRatingPreview() {
    ReportPreview(
        content = exampleForm(
            reason = IssueReason.RATING,
            expectation = ExpectationState.example(
                ratedAs = RatedAsState.example(rate = DietRate.NOT_ADVISED, rating = Rating.Negative),
                selected = DietRate.NEUTRAL,
            ),
        ),
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportNutritionPreview() {
    ReportPreview(
        content = exampleForm(
            reason = IssueReason.NUTRITION,
            details = "The label says 4 g carbs per 100 g.",
        ),
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportSendingPreview() {
    ReportPreview(
        content = exampleForm(
            reason = IssueReason.NUTRITION,
            details = "The label says 4 g carbs per 100 g.",
            isSending = true,
        ),
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportSentPreview() {
    ReportPreview(content = Sent)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun IssueReportFailedPreview() {
    ReportPreview(content = Failed)
}
