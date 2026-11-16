package pl.morning.tower.karbo.feature.report.ui.capture.event

internal sealed interface PhotoCaptureEvent {
    data object RequestCameraPermission : PhotoCaptureEvent
    data object OpenAppSettings : PhotoCaptureEvent
    data object OpenPhotoPicker : PhotoCaptureEvent
    data class TakePicture(val uri: String) : PhotoCaptureEvent
}
