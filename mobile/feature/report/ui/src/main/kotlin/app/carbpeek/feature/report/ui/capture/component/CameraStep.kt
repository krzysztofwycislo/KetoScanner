package app.carbpeek.feature.report.ui.capture.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.carbpeek.core.designsystem.component.CameraHint
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.report.ui.R
import app.carbpeek.feature.report.ui.camera.CameraViewfinder
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnGalleryClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnShutterClicked
import app.carbpeek.feature.report.ui.capture.state.CaptureStepState
import app.carbpeek.photocamera.PhotoCameraState

private const val FRAME_ASPECT_RATIO = 5f / 7f

@Composable
internal fun CameraStep(
    step: CaptureStepState.Camera,
    camera: PhotoCameraState,
    innerPadding: PaddingValues,
    onAction: (PhotoCaptureAction) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraViewfinder(
            camera = camera,
            torchEnabled = step.isTorchOn,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            TopRowSpacer()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.m),
                verticalArrangement = Arrangement.spacedBy(
                    space = AppTheme.spacing.m,
                    alignment = Alignment.CenterVertically,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CaptureFrame(modifier = Modifier.weight(weight = 1f, fill = false))
                Ui.CameraHint(text = step.frameHint.resolve())
            }
            BottomRow(
                isShutterEnabled = step.isShutterEnabled,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun CaptureFrame(modifier: Modifier) {
    val dimColor = AppTheme.colors.camera.dim
    val strokeColor = AppTheme.colors.text.inverse
    val strokeWidth = AppTheme.borders.highlightRing
    val shape = AppTheme.shapes.card
    val windowSize = LocalWindowInfo.current.containerSize
    Box(
        modifier = modifier
            .aspectRatio(FRAME_ASPECT_RATIO)
            .drawBehind {
                val outline = shape.createOutline(size = size, layoutDirection = layoutDirection, density = this)
                val frame = Path().apply { addOutline(outline) }
                val extent = maxOf(windowSize.width, windowSize.height).toFloat()
                clipPath(path = frame, clipOp = ClipOp.Difference) {
                    drawRect(
                        color = dimColor,
                        topLeft = Offset(x = -extent, y = -extent),
                        size = Size(width = size.width + extent * 2, height = size.height + extent * 2),
                    )
                }
                drawPath(path = frame, color = strokeColor, style = Stroke(width = strokeWidth.toPx()))
            },
    )
}

@Composable
private fun BottomRow(isShutterEnabled: Boolean, onAction: (PhotoCaptureAction) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.l, vertical = AppTheme.spacing.xl),
    ) {
        GalleryButton(
            onClick = { onAction(OnGalleryClicked) },
            modifier = Modifier.align(Alignment.CenterStart),
        )
        ShutterButton(
            isEnabled = isShutterEnabled,
            onClick = { onAction(OnShutterClicked) },
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun GalleryButton(onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = AppTheme.sizes.control.m)
            .clip(AppTheme.shapes.pill)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.report_from_gallery),
            style = AppTheme.typography.bodyStrong,
            color = AppTheme.colors.text.inverse,
        )
    }
}

@Composable
private fun ShutterButton(isEnabled: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val label = stringResource(R.string.report_take_photo)
    Box(
        modifier = modifier
            .size(AppTheme.sizes.control.xl)
            .clip(AppTheme.shapes.circle)
            .border(
                width = AppTheme.borders.scanButtonRing,
                color = AppTheme.colors.text.inverse,
                shape = AppTheme.shapes.circle,
            )
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.iconDiscCompact)
                .background(
                    color = if (isEnabled) AppTheme.colors.text.inverse else AppTheme.colors.camera.control,
                    shape = AppTheme.shapes.circle,
                ),
        )
    }
}
