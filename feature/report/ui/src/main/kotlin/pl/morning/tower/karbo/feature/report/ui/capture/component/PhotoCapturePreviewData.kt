package pl.morning.tower.karbo.feature.report.ui.capture.component

import pl.morning.tower.karbo.feature.report.ui.capture.PhotoCaptureResources
import pl.morning.tower.karbo.feature.report.ui.capture.state.CameraPermission
import pl.morning.tower.karbo.feature.report.ui.capture.state.CaptureStepState
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCaptureRequest
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCaptureState
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoSubject
import pl.morning.tower.karbo.feature.report.ui.capture.state.TorchButtonState

private val resources = PhotoCaptureResources()

internal fun PhotoCaptureState.Companion.example(
    step: CaptureStepState,
    namesSubject: Boolean = false,
) = PhotoCaptureState(
    counter = resources.counter(
        PhotoCaptureRequest(
            number = 2,
            total = 3,
            subject = PhotoSubject.NUTRITION_TABLE,
            namesSubject = namesSubject,
        ),
    ),
    step = step,
)

internal fun CaptureStepState.Camera.Companion.example(
    hasTorch: Boolean = true,
    isTorchOn: Boolean = false,
    isCapturing: Boolean = false,
) = CaptureStepState.Camera(
    frameHint = resources.frameHint(PhotoSubject.NUTRITION_TABLE),
    torchButton = TorchButtonState(
        isActive = isTorchOn,
        contentDescription = resources.torchLabel(isTorchOn),
    ).takeIf { hasTorch },
    isTorchOn = hasTorch && isTorchOn,
    isShutterEnabled = !isCapturing,
)

internal fun CaptureStepState.Preview.Companion.example() = CaptureStepState.Preview(
    imageUri = "",
    title = resources.previewTitle(PhotoSubject.NUTRITION_TABLE),
    body = resources.previewBody(PhotoSubject.NUTRITION_TABLE),
)

internal fun CaptureStepState.Blocked.Companion.example(permission: CameraPermission) = CaptureStepState.Blocked(
    title = resources.blockedTitle(permission),
    body = resources.blockedBody(permission),
    primaryLabel = resources.blockedButton(permission),
)
