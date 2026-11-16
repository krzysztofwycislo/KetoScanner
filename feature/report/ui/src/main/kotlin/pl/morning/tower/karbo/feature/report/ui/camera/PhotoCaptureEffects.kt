package pl.morning.tower.karbo.feature.report.ui.camera

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleStartEffect
import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.designsystem.event.ObserveEvents
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraChecked
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraPermissionResult
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnCancelClicked
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnPhotoPicked
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureFailed
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureSaved
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureEvent
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureEvent.OpenAppSettings
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureEvent.OpenPhotoPicker
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureEvent.RequestCameraPermission
import pl.morning.tower.karbo.feature.report.ui.capture.event.PhotoCaptureEvent.TakePicture
import pl.morning.tower.karbo.photocamera.PhotoCameraState

@Composable
internal fun PhotoCaptureEffects(
    isOpen: Boolean,
    events: Flow<PhotoCaptureEvent>,
    camera: PhotoCameraState,
    onAction: (PhotoCaptureAction) -> Unit,
) {
    val currentOnAction by rememberUpdatedState(onAction)
    val permissionHandler = rememberCameraPermissionHandler { check ->
        currentOnAction(OnCameraPermissionResult(check))
    }
    val photoPicker = rememberPhotoPicker { uri -> currentOnAction(OnPhotoPicked(uri)) }
    val hasTorch = rememberHasTorch()

    LifecycleStartEffect(isOpen) {
        if (isOpen) {
            currentOnAction(OnCameraChecked(check = permissionHandler.check(), hasTorch = hasTorch))
        }
        onStopOrDispose {}
    }

    ObserveEvents(events) { event ->
        when (event) {
            RequestCameraPermission -> permissionHandler.request()
            OpenAppSettings -> permissionHandler.openSettings()
            OpenPhotoPicker -> photoPicker.launch()
            is TakePicture -> camera.takePicture(
                file = event.uri.toUri().toFile(),
                onSaved = { currentOnAction(OnPictureSaved(event.uri)) },
                onFailed = { error ->
                    currentOnAction(OnPictureFailed(uri = event.uri, error = AppError.Unknown(error)))
                },
            )
        }
    }

    BackHandler(enabled = isOpen) {
        currentOnAction(OnCancelClicked)
    }
}
