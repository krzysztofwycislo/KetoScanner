package app.carbpeek.feature.report.ui.capture.state

import androidx.compose.runtime.Immutable
import app.carbpeek.feature.report.api.model.ReportPhoto

@Immutable
internal data class PhotoCapturePrototype(
    val isOpen: Boolean = false,
    val cameraPermission: CameraPermission = CameraPermission.Unchecked,
    val hasTorch: Boolean = false,
    val isTorchOn: Boolean = false,
    val isCapturing: Boolean = false,
    val previewPhoto: ReportPhoto? = null,
) {

    val photos: List<ReportPhoto> get() = listOfNotNull(previewPhoto)
}
