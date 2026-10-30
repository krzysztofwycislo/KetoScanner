package pl.handsome.club.feature.scanner.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.text.UiText

@Immutable
internal data class ScanState(
    val query: String,
    val showProgress: Boolean,
    val camera: CameraState,
    val found: FoundCardState?,
)

@Immutable
internal sealed interface CameraState {
    data object Hidden : CameraState
    data class Preview(
        val isPaused: Boolean,
        val torch: Torch,
        val isTypeSheetOpen: Boolean,
        val trip: ImmutableList<TripItemState>,
    ) : CameraState
    data class PermissionPanel(val title: UiText, val message: UiText, val buttonLabel: UiText) : CameraState
}

@Immutable
internal data class FoundCardState(
    val name: UiText,
    val brand: UiText?,
    val rating: Rating,
    val title: UiText,
    val detail: UiText,
)

@Immutable
internal data class TripItemState(
    val barcode: String,
    val name: UiText,
    val rating: Rating,
    val carbs: UiText,
    val description: UiText,
)

internal enum class Torch { Unavailable, Off, On }
