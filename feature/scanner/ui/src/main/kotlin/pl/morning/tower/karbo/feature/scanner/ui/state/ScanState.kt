package pl.morning.tower.karbo.feature.scanner.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.component.ScanFrameMode
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.text.UiText

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
        val hint: CameraHintState,
    ) : CameraState
    data class PermissionPanel(val title: UiText, val message: UiText, val buttonLabel: UiText) : CameraState
}

@Immutable
internal data class CameraHintState(val text: UiText, val supportingText: UiText?)

@Immutable
internal data class FoundCardState(
    val name: UiText,
    val icon: AppIcon,
    val brand: UiText?,
    val rating: Rating,
    val title: UiText,
    val detail: UiText,
    val showVerdictTip: Boolean,
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
