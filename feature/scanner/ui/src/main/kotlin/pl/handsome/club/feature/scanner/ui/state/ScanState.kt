package pl.handsome.club.feature.scanner.ui.state

import androidx.compose.runtime.Immutable
import pl.handsome.club.core.designsystem.text.UiText

@Immutable
internal data class ScanState(
    val query: String,
    val showProgress: Boolean,
    val camera: CameraState,
)

@Immutable
internal sealed interface CameraState {
    data object Hidden : CameraState
    data class Preview(val isPaused: Boolean, val torch: Torch, val isTypeSheetOpen: Boolean) : CameraState
    data class PermissionPanel(val title: UiText, val message: UiText, val buttonLabel: UiText) : CameraState
}

internal enum class Torch { Unavailable, Off, On }
