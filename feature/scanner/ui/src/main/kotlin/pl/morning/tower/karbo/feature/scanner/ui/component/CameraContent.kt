package pl.morning.tower.karbo.feature.scanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.component.CameraHint
import pl.morning.tower.karbo.core.designsystem.component.CameraIconButton
import pl.morning.tower.karbo.core.designsystem.component.CameraPillButton
import pl.morning.tower.karbo.core.designsystem.component.ScanFrame
import pl.morning.tower.karbo.core.designsystem.component.ScanFrameMode
import pl.morning.tower.karbo.core.designsystem.component.Screen
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.scanner.ui.R
import pl.morning.tower.karbo.feature.scanner.ui.ScanScreenPreview
import pl.morning.tower.karbo.feature.scanner.ui.camera.CameraPreview
import pl.morning.tower.karbo.feature.scanner.ui.camera.LightStatusBarIconsEffect
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnBarcodeScanned
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnTorchClicked
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnTypeItClicked
import pl.morning.tower.karbo.feature.scanner.ui.state.CameraState
import pl.morning.tower.karbo.feature.scanner.ui.state.ScanState
import pl.morning.tower.karbo.feature.scanner.ui.state.TorchButtonState

private const val WIDE_FRAME_WIDTH_FRACTION = 0.5f

@Composable
internal fun CameraContent(state: ScanState, camera: CameraState.Preview, onAction: (ScanAction) -> Unit) {
    Ui.Screen(containerColor = AppTheme.colors.camera.background) { innerPadding ->
        LightStatusBarIconsEffect()
        var scanArea by remember { mutableStateOf<Rect?>(null) }
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                isPaused = camera.isPaused,
                torchEnabled = camera.isTorchOn,
                scanArea = scanArea,
                onBarcodeScanned = { barcode -> onAction(OnBarcodeScanned(barcode)) },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("cameraPreviewView"),
            )
            val frameModifier = Modifier.onGloballyPositioned { scanArea = it.boundsInWindow() }
            val hint = camera.hint.resolve()
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (maxWidth > maxHeight) {
                    WideCameraLayout(camera, hint, frameModifier, onAction)
                } else {
                    TallCameraLayout(camera, hint, frameModifier, onAction)
                }
            }
            TypeSheet(
                query = state.query,
                isLookUpEnabled = state.isLookUpEnabled,
                visible = camera.isTypeSheetOpen,
                innerPadding = innerPadding,
                onAction = onAction,
            )
            FoundOverlay(state.found, innerPadding, onAction)
            NotFoundOverlay(state.notFound, innerPadding, onAction)
        }
    }
}

@Composable
private fun BoxScope.TallCameraLayout(
    camera: CameraState.Preview,
    hint: String,
    frameModifier: Modifier,
    onAction: (ScanAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CameraHeader(camera.torchButton, onAction)
        Spacer(Modifier.height(AppTheme.spacing.xl * 2))
        Ui.ScanFrame(
            mode = camera.frame,
            modifier = frameModifier,
        )
        Ui.CameraHint(
            text = hint,
            modifier = Modifier.padding(top = AppTheme.spacing.m),
        )
    }
    TripOverlay(
        trip = camera.trip,
        vertical = false,
        onAction = onAction,
        stripModifier = Modifier.padding(
            start = AppTheme.spacing.m,
            end = AppTheme.spacing.m,
            bottom = AppTheme.spacing.s,
        ),
    )
}

@Composable
private fun WideCameraLayout(
    camera: CameraState.Preview,
    hint: String,
    frameModifier: Modifier,
    onAction: (ScanAction) -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
            horizontalAlignment = Alignment.Start,
        ) {
            TorchButton(camera.torchButton, onAction)
            TypeItButton(onAction)
            Ui.CameraHint(text = hint)
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(WIDE_FRAME_WIDTH_FRACTION)
                .padding(vertical = AppTheme.spacing.m),
            contentAlignment = Alignment.Center,
        ) {
            Ui.ScanFrame(
                mode = camera.frame,
                modifier = frameModifier,
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(AppTheme.spacing.m),
        ) {
            TripOverlay(
                trip = camera.trip,
                vertical = true,
                onAction = onAction,
                stripModifier = Modifier,
            )
        }
    }
}

@Composable
private fun CameraHeader(torch: TorchButtonState?, onAction: (ScanAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.l),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TorchButton(torch, onAction)
        TypeItButton(onAction)
    }
}

@Composable
private fun TorchButton(torch: TorchButtonState?, onAction: (ScanAction) -> Unit) {
    if (torch == null) {
        Spacer(Modifier)
    } else {
        Ui.CameraIconButton(
            icon = AppIcons.Torch.imageVector,
            contentDescription = torch.contentDescription,
            active = torch.isActive,
            onClick = { onAction(OnTorchClicked) },
        )
    }
}

@Composable
private fun TypeItButton(onAction: (ScanAction) -> Unit) {
    Ui.CameraPillButton(
        text = stringResource(R.string.scan_type_it),
        icon = AppIcons.Keyboard.imageVector,
        onClick = { onAction(OnTypeItClicked) },
    )
}

@Preview
@Composable
private fun ScanAimingPreview() {
    ScanScreenPreview(previewState())
}

@Preview
@Composable
private fun ScanTorchOnPreview() {
    ScanScreenPreview(
        previewState(
            isTorchOn = true,
            torchButton = TorchButtonState(isActive = true, contentDescription = previewResources.torchOff()),
        ),
    )
}

@Preview
@Composable
private fun ScanTorchUnavailablePreview() {
    ScanScreenPreview(previewState(torchButton = null))
}

@Preview
@Composable
private fun ScanSearchingCameraPreview() {
    ScanScreenPreview(
        previewState(
            isLookUpEnabled = false,
            isPaused = true,
            frame = ScanFrameMode.Searching,
            hint = previewResources.searchingHint(),
        ),
    )
}

@Preview(widthDp = 891, heightDp = 411)
@Composable
private fun ScanAimingLandscapePreview() {
    ScanAimingPreview()
}
