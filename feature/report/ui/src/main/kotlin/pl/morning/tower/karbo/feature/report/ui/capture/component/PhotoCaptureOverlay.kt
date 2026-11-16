package pl.morning.tower.karbo.feature.report.ui.capture.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.component.CameraIconButton
import pl.morning.tower.karbo.core.designsystem.component.Screen
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.report.ui.R
import pl.morning.tower.karbo.feature.report.ui.camera.LightStatusBarIconsEffect
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnCancelClicked
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnTorchClicked
import pl.morning.tower.karbo.feature.report.ui.capture.state.CameraPermission
import pl.morning.tower.karbo.feature.report.ui.capture.state.CaptureStepState
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCaptureState
import pl.morning.tower.karbo.feature.report.ui.capture.state.TorchButtonState
import pl.morning.tower.karbo.photocamera.PhotoCameraState
import pl.morning.tower.karbo.photocamera.rememberPhotoCameraState

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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CancelButton(onClick = { onAction(OnCancelClicked) })
        CounterChip(counter)
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
            .height(AppTheme.sizes.icon.xl)
            .clip(AppTheme.shapes.pill)
            .background(AppTheme.colors.camera.chip)
            .padding(horizontal = AppTheme.spacing.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = counter.resolve(),
            style = AppTheme.typography.bodyStrong,
            color = AppTheme.colors.text.inverse,
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
private fun OverlayPreview(step: CaptureStepState) {
    AppTheme {
        PhotoCaptureOverlay(
            state = PhotoCaptureState.example(step),
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
