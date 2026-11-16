package pl.morning.tower.karbo.feature.report.ui.capture.event

import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.feature.report.ui.capture.state.CameraPermissionCheck

internal sealed interface PhotoCaptureAction {
    data class OnCameraChecked(val check: CameraPermissionCheck, val hasTorch: Boolean) : PhotoCaptureAction
    data class OnCameraPermissionResult(val check: CameraPermissionCheck) : PhotoCaptureAction
    data object OnPermissionButtonClicked : PhotoCaptureAction
    data object OnTorchClicked : PhotoCaptureAction
    data object OnShutterClicked : PhotoCaptureAction
    data class OnPictureSaved(val uri: String) : PhotoCaptureAction
    data class OnPictureFailed(val uri: String, val error: AppError) : PhotoCaptureAction
    data object OnGalleryClicked : PhotoCaptureAction
    data class OnPhotoPicked(val uri: String) : PhotoCaptureAction
    data object OnRetakeClicked : PhotoCaptureAction
    data object OnUsePhotoClicked : PhotoCaptureAction
    data object OnCancelClicked : PhotoCaptureAction
}
