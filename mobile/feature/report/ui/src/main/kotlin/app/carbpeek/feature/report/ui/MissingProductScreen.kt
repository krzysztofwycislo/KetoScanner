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
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.report.ui.camera.PhotoCaptureEffects
import app.carbpeek.feature.report.ui.capture.component.PhotoCaptureOverlay
import app.carbpeek.feature.report.ui.capture.component.example
import app.carbpeek.feature.report.ui.capture.state.CaptureStepState
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureState
import app.carbpeek.feature.report.ui.component.FailedContent
import app.carbpeek.feature.report.ui.component.MissingProductForm
import app.carbpeek.feature.report.ui.component.SentContent
import app.carbpeek.feature.report.ui.component.exampleMissingProductForm
import app.carbpeek.feature.report.ui.event.MissingProductAction
import app.carbpeek.feature.report.ui.event.MissingProductAction.OnBackToReportClicked
import app.carbpeek.feature.report.ui.event.MissingProductAction.OnCapture
import app.carbpeek.feature.report.ui.event.MissingProductAction.OnHomeClicked
import app.carbpeek.feature.report.ui.event.MissingProductAction.OnScanNextClicked
import app.carbpeek.feature.report.ui.event.MissingProductAction.OnTryAgainClicked
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation
import app.carbpeek.feature.report.ui.state.MissingProductContentState
import app.carbpeek.feature.report.ui.state.MissingProductContentState.Failed
import app.carbpeek.feature.report.ui.state.MissingProductContentState.Form
import app.carbpeek.feature.report.ui.state.MissingProductContentState.Sent
import app.carbpeek.feature.report.ui.state.MissingProductState
import app.carbpeek.feature.report.ui.state.PhotoSlot
import app.carbpeek.photocamera.PhotoCameraState
import app.carbpeek.photocamera.rememberPhotoCameraState
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MissingProductScreen(onNavigation: (ReportNavigation) -> Unit) {
    val viewModel: MissingProductViewModel = koinViewModel()

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
    state: MissingProductState,
    camera: PhotoCameraState,
    onAction: (MissingProductAction) -> Unit,
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
                    is Form -> MissingProductForm(content, onAction)
                    Sent -> {
                        SentContent(
                            title = stringResource(R.string.report_missing_sent_title),
                            body = stringResource(R.string.report_missing_sent_body),
                            steps = persistentListOf(
                                stringResource(R.string.report_step_photos_sent),
                                stringResource(R.string.report_step_read_label),
                                stringResource(R.string.report_step_approve),
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
private fun MissingProductPreview(content: MissingProductContentState, photoCapture: PhotoCaptureState? = null) {
    AppTheme {
        ScreenContent(
            state = MissingProductState(content = content, photoCapture = photoCapture),
            camera = rememberPhotoCameraState(),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MissingProductEmptyPreview() {
    MissingProductPreview(content = exampleMissingProductForm())
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1200, fontScale = 1.5f)
@Composable
private fun MissingProductEmptyLargeFontPreview() {
    MissingProductEmptyPreview()
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MissingProductFrontOnlyPreview() {
    MissingProductPreview(content = exampleMissingProductForm(filled = setOf(PhotoSlot.FRONT)))
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MissingProductReadyPreview() {
    MissingProductPreview(
        content = exampleMissingProductForm(filled = setOf(PhotoSlot.FRONT, PhotoSlot.NUTRITION_TABLE)),
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MissingProductSendingPreview() {
    MissingProductPreview(
        content = exampleMissingProductForm(
            filled = setOf(PhotoSlot.FRONT, PhotoSlot.NUTRITION_TABLE),
            isSending = true,
        ),
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MissingProductPhotoCapturePreview() {
    MissingProductPreview(
        content = exampleMissingProductForm(filled = setOf(PhotoSlot.FRONT)),
        photoCapture = PhotoCaptureState.example(
            step = CaptureStepState.Camera.example(),
            namesSubject = true,
        ),
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MissingProductSentPreview() {
    MissingProductPreview(content = Sent)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun MissingProductFailedPreview() {
    MissingProductPreview(content = Failed)
}
