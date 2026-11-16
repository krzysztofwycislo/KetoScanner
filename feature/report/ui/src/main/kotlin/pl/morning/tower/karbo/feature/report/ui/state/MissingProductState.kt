package pl.morning.tower.karbo.feature.report.ui.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCaptureState

@Immutable
internal data class MissingProductState(
    val content: MissingProductContentState,
    val photoCapture: PhotoCaptureState?,
)

@Immutable
internal sealed interface MissingProductContentState {

    data class Form(
        val barcode: UiText,
        val slots: PersistentList<PhotoSlotState>,
        val isEditable: Boolean,
        val sendButton: SendButtonState,
    ) : MissingProductContentState

    data object Sent : MissingProductContentState

    data object Failed : MissingProductContentState
}

@Immutable
internal data class PhotoSlotState(
    val slot: PhotoSlot,
    val label: UiText,
    val note: UiText,
    val tag: UiText,
    val tagTone: SlotTagTone,
    val photoUri: String?,
    val actionLabel: UiText,
) {
    companion object
}

internal enum class SlotTagTone {
    REQUIRED,
    RECOMMENDED,
}
