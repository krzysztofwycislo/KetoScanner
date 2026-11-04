package pl.morning.tower.karbo.feature.scanner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.morning.tower.karbo.core.designsystem.component.Screen
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.event.ObserveEvents
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.scanner.ui.camera.LightStatusBarIconsEffect
import pl.morning.tower.karbo.feature.scanner.ui.camera.rememberCameraPermissionHandler
import pl.morning.tower.karbo.feature.scanner.ui.camera.rememberHasTorch
import pl.morning.tower.karbo.feature.scanner.ui.component.CameraContent
import pl.morning.tower.karbo.feature.scanner.ui.component.PermissionContent
import pl.morning.tower.karbo.feature.scanner.ui.event.OpenAppSettings
import pl.morning.tower.karbo.feature.scanner.ui.event.PerformHapticFeedback
import pl.morning.tower.karbo.feature.scanner.ui.event.RequestCameraPermission
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnCameraPermissionResult
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanAction.OnStart
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanEvent.ScanNavigation
import pl.morning.tower.karbo.feature.scanner.ui.event.ScanFeedback
import pl.morning.tower.karbo.feature.scanner.ui.state.CameraState
import pl.morning.tower.karbo.feature.scanner.ui.state.ScanState

@Composable
internal fun ScanScreen(onNavigation: (ScanNavigation) -> Unit) {
    val viewModel: ScanViewModel = koinViewModel()
    val permissionHandler = rememberCameraPermissionHandler { check ->
        viewModel.onAction(OnCameraPermissionResult(check))
    }
    val hasTorch = rememberHasTorch()

    LifecycleStartEffect(Unit) {
        viewModel.onAction(OnStart(permissionHandler.check(), hasTorch))
        onStopOrDispose {}
    }

    val haptics = LocalHapticFeedback.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is ScanNavigation -> onNavigation(event)
            RequestCameraPermission -> permissionHandler.request()
            OpenAppSettings -> permissionHandler.openSettings()
            is PerformHapticFeedback -> haptics.performHapticFeedback(event.feedback.hapticType())
        }
    }

    ScreenContent(state, viewModel::onAction)
}

private fun ScanFeedback.hapticType(): HapticFeedbackType = when (this) {
    ScanFeedback.Detected -> HapticFeedbackType.SegmentTick
    ScanFeedback.Found -> HapticFeedbackType.Confirm
    ScanFeedback.NotFound -> HapticFeedbackType.Reject
}

@Composable
private fun ScreenContent(state: ScanState, onAction: (ScanAction) -> Unit) {
    when (val camera = state.camera) {
        is CameraState.Preview -> CameraContent(state, camera, onAction)
        is CameraState.PermissionPanel -> PermissionContent(state, camera, onAction)
        CameraState.Hidden -> Ui.Screen(containerColor = AppTheme.colors.camera.background) {
            LightStatusBarIconsEffect()
        }
    }
}

@Composable
internal fun ScanScreenPreview(state: ScanState) {
    AppTheme {
        ScreenContent(state) {}
    }
}

@Preview
@Composable
private fun ScanHiddenPreview() {
    ScanScreenPreview(
        ScanState(query = "", isLookUpEnabled = true, camera = CameraState.Hidden, found = null, notFound = null),
    )
}
