package app.carbpeek.feature.report.ui.camera

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleStartEffect
import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraChecked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraPermissionResult
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCancelClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPhotoPicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureFailed
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureSaved
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.OpenAppSettings
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.OpenPhotoPicker
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.RequestCameraPermission
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.TakePicture
import app.carbpeek.photocamera.PhotoCameraState
import kotlinx.coroutines.flow.Flow

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
