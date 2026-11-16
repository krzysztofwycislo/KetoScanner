package app.carbpeek.feature.report.ui.component

import app.carbpeek.feature.report.ui.MissingProductResources
import app.carbpeek.feature.report.ui.state.MissingProductContentState
import app.carbpeek.feature.report.ui.state.PhotoSlot
import app.carbpeek.feature.report.ui.state.PhotoSlotState
import app.carbpeek.feature.report.ui.state.SendButtonState
import app.carbpeek.feature.report.ui.state.SlotTagTone
import kotlinx.collections.immutable.toPersistentList

private val resources = MissingProductResources()

internal fun PhotoSlotState.Companion.example(slot: PhotoSlot, hasPhoto: Boolean = false) = PhotoSlotState(
    slot = slot,
    label = resources.slotLabel(slot),
    note = resources.slotNote(slot),
    tag = resources.slotTag(slot),
    tagTone = if (slot.isRequired) SlotTagTone.REQUIRED else SlotTagTone.RECOMMENDED,
    photoUri = "".takeIf { hasPhoto },
    actionLabel = resources.slotAction(hasPhoto),
)

internal fun exampleMissingProductForm(
    filled: Set<PhotoSlot> = emptySet(),
    isSending: Boolean = false,
): MissingProductContentState.Form {
    val isReady = PhotoSlot.entries.filter { it.isRequired }.all { it in filled }
    return MissingProductContentState.Form(
        barcode = resources.barcode("5900512300320"),
        slots = PhotoSlot.entries.map { slot ->
            PhotoSlotState.example(slot = slot, hasPhoto = slot in filled)
        }.toPersistentList(),
        isEditable = !isSending,
        sendButton = SendButtonState(
            label = if (isReady || isSending) resources.sendLabel() else resources.addRequiredPhotosLabel(),
            isEnabled = isReady || isSending,
            isLoading = isSending,
        ),
    )
}
