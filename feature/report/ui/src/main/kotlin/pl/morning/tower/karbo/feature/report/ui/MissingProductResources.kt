package pl.morning.tower.karbo.feature.report.ui

import pl.morning.tower.karbo.core.designsystem.text.UiArg
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.report.ui.state.PhotoSlot

internal class MissingProductResources {

    fun barcode(barcode: String): UiText = UiText.Res(R.string.report_missing_barcode, UiArg.Text(barcode))

    fun slotLabel(slot: PhotoSlot): UiText = UiText.Res(
        when (slot) {
            PhotoSlot.FRONT -> R.string.report_slot_front
            PhotoSlot.NUTRITION_TABLE -> R.string.report_slot_nutrition
            PhotoSlot.INGREDIENTS -> R.string.report_slot_ingredients
        },
    )

    fun slotNote(slot: PhotoSlot): UiText = UiText.Res(
        when (slot) {
            PhotoSlot.FRONT -> R.string.report_slot_front_note
            PhotoSlot.NUTRITION_TABLE -> R.string.report_slot_nutrition_note
            PhotoSlot.INGREDIENTS -> R.string.report_slot_ingredients_note
        },
    )

    fun slotTag(slot: PhotoSlot): UiText =
        UiText.Res(if (slot.isRequired) R.string.report_slot_required else R.string.report_slot_recommended)

    fun slotAction(hasPhoto: Boolean): UiText =
        UiText.Res(if (hasPhoto) R.string.report_retake else R.string.report_take_photo)

    fun sendLabel(): UiText = UiText.Res(R.string.report_send_photos)

    fun addRequiredPhotosLabel(): UiText = UiText.Res(R.string.report_add_required_photos)
}
