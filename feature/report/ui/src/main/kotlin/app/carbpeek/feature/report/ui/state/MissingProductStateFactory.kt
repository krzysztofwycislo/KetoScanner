package app.carbpeek.feature.report.ui.state

import app.carbpeek.feature.report.ui.MissingProductResources
import app.carbpeek.feature.report.ui.capture.state.PhotoCapturePrototype
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureStateFactory
import app.carbpeek.feature.report.ui.state.MissingProductContentState.Failed
import app.carbpeek.feature.report.ui.state.MissingProductContentState.Form
import app.carbpeek.feature.report.ui.state.MissingProductContentState.Sent
import kotlinx.collections.immutable.toPersistentList
import app.carbpeek.feature.report.ui.state.MissingProductStatePrototype as Prototype

internal class MissingProductStateFactory(
    private val resources: MissingProductResources,
    private val captureStateFactory: PhotoCaptureStateFactory,
) {

    fun createFor(prototype: Prototype, capture: PhotoCapturePrototype): MissingProductState = MissingProductState(
        content = prototype.content(),
        photoCapture = if (prototype.status == ReportStatus.EDITING) {
            captureStateFactory.createFor(capture = capture, request = prototype.captureRequest())
        } else {
            null
        },
    )

    private fun Prototype.content(): MissingProductContentState = when (status) {
        ReportStatus.SENT -> Sent
        ReportStatus.FAILED -> Failed
        ReportStatus.EDITING, ReportStatus.SENDING -> Form(
            barcode = resources.barcode(barcode.value),
            slots = slots(),
            isEditable = status == ReportStatus.EDITING,
            sendButton = sendButton(),
        )
    }

    private fun Prototype.slots() = PhotoSlot.entries.map { slot ->
        val photo = slotPhotos[slot]
        PhotoSlotState(
            slot = slot,
            label = resources.slotLabel(slot),
            note = resources.slotNote(slot),
            tag = resources.slotTag(slot),
            tagTone = if (slot.isRequired) SlotTagTone.REQUIRED else SlotTagTone.RECOMMENDED,
            photoUri = photo?.uri,
            actionLabel = resources.slotAction(hasPhoto = photo != null),
        )
    }.toPersistentList()

    private fun Prototype.sendButton(): SendButtonState = when {
        status == ReportStatus.SENDING -> SendButtonState(
            label = resources.sendLabel(),
            isEnabled = true,
            isLoading = true,
        )
        !canSend -> SendButtonState(
            label = resources.addRequiredPhotosLabel(),
            isEnabled = false,
            isLoading = false,
        )
        else -> SendButtonState(
            label = resources.sendLabel(),
            isEnabled = true,
            isLoading = false,
        )
    }
}
