package app.carbpeek.feature.report.ui.capture.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.CameraIconButton
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.report.ui.R
import app.carbpeek.feature.report.ui.camera.LightStatusBarIconsEffect
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCancelClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnTorchClicked
import app.carbpeek.feature.report.ui.capture.state.CameraPermission
import app.carbpeek.feature.report.ui.capture.state.CaptureStepState
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureState
import app.carbpeek.feature.report.ui.capture.state.TorchButtonState
import app.carbpeek.photocamera.PhotoCameraState
import app.carbpeek.photocamera.rememberPhotoCameraState

@Composable
internal fun PhotoCaptureOverlay(
    state: PhotoCaptureState,
    camera: PhotoCameraState,
    onAction: (PhotoCaptureAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Ui.Screen(
        modifier = modifier,
        containerColor = AppTheme.colors.camera.background,
    ) { innerPadding ->
        LightStatusBarIconsEffect()
        Box(modifier = Modifier.fillMaxSize()) {
            when (val step = state.step) {
                CaptureStepState.Waiting -> Unit
                is CaptureStepState.Camera -> {
                    CameraStep(
                        step = step,
                        camera = camera,
                        innerPadding = innerPadding,
                        onAction = onAction,
                    )
                }
                is CaptureStepState.Preview -> {
                    PreviewStep(step = step, innerPadding = innerPadding, onAction = onAction)
                }
                is CaptureStepState.Blocked -> {
                    BlockedStep(step = step, innerPadding = innerPadding, onAction = onAction)
                }
            }
            TopRow(
                counter = state.counter,
                torchButton = (state.step as? CaptureStepState.Camera)?.torchButton,
                onAction = onAction,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
internal fun TopRowSpacer() {
    Spacer(modifier = Modifier.height(AppTheme.sizes.control.m + AppTheme.spacing.l * 2))
}

@Composable
private fun TopRow(
    counter: UiText,
    torchButton: TorchButtonState?,
    onAction: (PhotoCaptureAction) -> Unit,
    modifier: Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.l),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CancelButton(onClick = { onAction(OnCancelClicked) })
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            CounterChip(counter)
        }
        if (torchButton == null) {
            Spacer(modifier = Modifier.size(AppTheme.sizes.control.m))
        } else {
            Ui.CameraIconButton(
                icon = AppIcons.Torch.imageVector,
                contentDescription = torchButton.contentDescription,
                active = torchButton.isActive,
                onClick = { onAction(OnTorchClicked) },
            )
        }
    }
}

@Composable
private fun CounterChip(counter: UiText) {
    Box(
        modifier = Modifier
            .heightIn(min = AppTheme.sizes.icon.xl)
            .clip(AppTheme.shapes.large)
            .background(AppTheme.colors.camera.chip)
            .padding(horizontal = AppTheme.spacing.s, vertical = AppTheme.spacing.xxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = counter.resolve(),
            style = AppTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.text.inverse,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CancelButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(AppTheme.sizes.control.m)
            .clip(AppTheme.shapes.circle)
            .background(AppTheme.colors.camera.control)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Close.imageVector,
            contentDescription = stringResource(R.string.report_cancel_photo),
            tint = AppTheme.colors.text.inverse,
            modifier = Modifier.size(AppTheme.sizes.icon.s),
        )
    }
}

@Composable
private fun OverlayPreview(step: CaptureStepState, namesSubject: Boolean = false) {
    AppTheme {
        PhotoCaptureOverlay(
            state = PhotoCaptureState.example(step = step, namesSubject = namesSubject),
            camera = rememberPhotoCameraState(),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PhotoCaptureWaitingPreview() {
    OverlayPreview(CaptureStepState.Waiting)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PhotoCaptureCameraPreview() {
    OverlayPreview(CaptureStepState.Camera.example())
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
private fun PhotoCaptureCameraLargeFontPreview() {
    OverlayPreview(step = CaptureStepState.Camera.example(), namesSubject = true)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PhotoCaptureTorchOnPreview() {
    OverlayPreview(CaptureStepState.Camera.example(isTorchOn = true))
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PhotoCaptureNoTorchPreview() {
    OverlayPreview(CaptureStepState.Camera.example(hasTorch = false))
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PhotoCaptureCapturingPreview() {
    OverlayPreview(CaptureStepState.Camera.example(isCapturing = true))
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PhotoCapturePreviewStepPreview() {
    OverlayPreview(CaptureStepState.Preview.example())
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, fontScale = 1.5f)
@Composable
private fun PhotoCapturePreviewStepLargeFontPreview() {
    OverlayPreview(step = CaptureStepState.Preview.example(), namesSubject = true)
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PhotoCaptureDeniedPreview() {
    OverlayPreview(CaptureStepState.Blocked.example(CameraPermission.Denied))
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun PhotoCaptureRationalePreview() {
    OverlayPreview(CaptureStepState.Blocked.example(CameraPermission.Rationale))
}
