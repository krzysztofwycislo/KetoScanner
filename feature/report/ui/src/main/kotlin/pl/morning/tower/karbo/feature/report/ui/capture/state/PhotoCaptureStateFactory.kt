package pl.morning.tower.karbo.feature.report.ui.capture.state

import pl.morning.tower.karbo.feature.report.ui.capture.PhotoCaptureResources

internal class PhotoCaptureStateFactory(private val resources: PhotoCaptureResources) {

    fun createFor(capture: PhotoCapturePrototype, request: PhotoCaptureRequest): PhotoCaptureState? {
        if (!capture.isOpen) return null
        return PhotoCaptureState(
            counter = resources.counter(request),
            step = capture.step(request.subject),
        )
    }

    private fun PhotoCapturePrototype.step(subject: PhotoSubject): CaptureStepState {
        val photo = previewPhoto
        return when {
            photo != null -> CaptureStepState.Preview(
                imageUri = photo.uri,
                title = resources.previewTitle(subject),
                body = resources.previewBody(subject),
            )
            else -> when (cameraPermission) {
                CameraPermission.Granted -> camera(subject)
                CameraPermission.Rationale, CameraPermission.Denied -> CaptureStepState.Blocked(
                    title = resources.blockedTitle(cameraPermission),
                    body = resources.blockedBody(cameraPermission),
                    primaryLabel = resources.blockedButton(cameraPermission),
                )
                CameraPermission.Unchecked, CameraPermission.Requesting -> CaptureStepState.Waiting
            }
        }
    }

    private fun PhotoCapturePrototype.camera(subject: PhotoSubject): CaptureStepState.Camera {
        val torchOn = hasTorch && isTorchOn
        return CaptureStepState.Camera(
            frameHint = resources.frameHint(subject),
            torchButton = if (hasTorch) {
                TorchButtonState(isActive = torchOn, contentDescription = resources.torchLabel(torchOn))
            } else {
                null
            },
            isTorchOn = torchOn,
            isShutterEnabled = !isCapturing,
        )
    }
}
