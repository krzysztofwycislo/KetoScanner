package pl.handsome.club.feature.scanner.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.ScanFrameMode
import pl.handsome.club.core.designsystem.icon.AppIcon
import pl.handsome.club.core.designsystem.text.UiText

@Immutable
internal data class ScanState(
    val query: String,
    val isLookUpEnabled: Boolean,
    val camera: CameraState,
    val found: FoundCardState?,
    val notFound: NotFoundCardState?,
)

@Immutable
internal sealed interface CameraState {
    data object Hidden : CameraState
    data class Preview(
        val isPaused: Boolean,
        val isTorchOn: Boolean,
        val torchButton: TorchButtonState?,
        val isTypeSheetOpen: Boolean,
        val trip: ImmutableList<TripItemState>,
        val frame: ScanFrameMode,
        val hint: UiText,
    ) : CameraState
    data class PermissionPanel(val title: UiText, val message: UiText, val buttonLabel: UiText) : CameraState
}

@Immutable
internal data class FoundCardState(
    val name: UiText,
    val icon: AppIcon,
    val brand: UiText?,
    val rating: Rating,
    val title: UiText,
    val detail: UiText,
)

@Immutable
internal data class NotFoundCardState(val message: UiText, val showTypeIt: Boolean)

@Immutable
internal data class TripItemState(
    val barcode: String,
    val name: UiText,
    val icon: AppIcon,
    val rating: Rating,
    val carbs: UiText,
    val description: UiText,
)

@Immutable
internal data class TorchButtonState(val isActive: Boolean, val contentDescription: UiText)
