package app.carbpeek.feature.scanner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.scanner.ui.camera.LightStatusBarIconsEffect
import app.carbpeek.feature.scanner.ui.camera.rememberCameraPermissionHandler
import app.carbpeek.feature.scanner.ui.camera.rememberHasTorch
import app.carbpeek.feature.scanner.ui.component.CameraContent
import app.carbpeek.feature.scanner.ui.component.PermissionContent
import app.carbpeek.feature.scanner.ui.event.OpenAppSettings
import app.carbpeek.feature.scanner.ui.event.PerformHapticFeedback
import app.carbpeek.feature.scanner.ui.event.RequestCameraPermission
import app.carbpeek.feature.scanner.ui.event.ScanAction
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnCameraPermissionResult
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnStart
import app.carbpeek.feature.scanner.ui.event.ScanAction.OnStop
import app.carbpeek.feature.scanner.ui.event.ScanEvent.ScanNavigation
import app.carbpeek.feature.scanner.ui.event.ScanFeedback
import app.carbpeek.feature.scanner.ui.state.CameraState
import app.carbpeek.feature.scanner.ui.state.ScanState
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ScanScreen(onNavigation: (ScanNavigation) -> Unit) {
    val viewModel: ScanViewModel = koinViewModel()
    val permissionHandler = rememberCameraPermissionHandler { check ->
        viewModel.onAction(OnCameraPermissionResult(check))
    }
    val hasTorch = rememberHasTorch()

    LifecycleStartEffect(Unit) {
        viewModel.onAction(OnStart(permissionHandler.check(), hasTorch))
        onStopOrDispose { viewModel.onAction(OnStop) }
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
