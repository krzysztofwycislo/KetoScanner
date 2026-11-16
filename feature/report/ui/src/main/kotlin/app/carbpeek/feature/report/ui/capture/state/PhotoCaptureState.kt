package app.carbpeek.feature.report.ui.capture.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.designsystem.text.UiText

@Immutable
internal data class PhotoCaptureState(
    val counter: UiText,
    val step: CaptureStepState,
) {
    companion object
}

@Immutable
internal sealed interface CaptureStepState {

    data object Waiting : CaptureStepState

    data class Camera(
        val frameHint: UiText,
        val torchButton: TorchButtonState?,
        val isTorchOn: Boolean,
        val isShutterEnabled: Boolean,
    ) : CaptureStepState {
        companion object
    }

    data class Preview(
        val imageUri: String,
        val title: UiText,
        val body: UiText,
    ) : CaptureStepState {
        companion object
    }

    data class Blocked(
        val title: UiText,
        val body: UiText,
        val primaryLabel: UiText,
    ) : CaptureStepState {
        companion object
    }
}

@Immutable
internal data class TorchButtonState(
    val isActive: Boolean,
    val contentDescription: UiText,
) {
    companion object
}
