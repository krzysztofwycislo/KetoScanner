package pl.morning.tower.karbo.feature.report.ui

import pl.morning.tower.karbo.core.designsystem.text.UiArg
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto
import pl.morning.tower.karbo.feature.report.ui.capture.PhotoCaptureResources
import pl.morning.tower.karbo.feature.report.ui.capture.state.CameraPermission
import pl.morning.tower.karbo.feature.report.ui.capture.state.CaptureStepState
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCapturePrototype
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCaptureStateFactory
import pl.morning.tower.karbo.feature.report.ui.state.MissingProductContentState
import pl.morning.tower.karbo.feature.report.ui.state.MissingProductState
import pl.morning.tower.karbo.feature.report.ui.state.MissingProductStateFactory
import pl.morning.tower.karbo.feature.report.ui.state.MissingProductStatePrototype
import pl.morning.tower.karbo.feature.report.ui.state.PhotoSlot
import pl.morning.tower.karbo.feature.report.ui.state.PhotoSlotState
import pl.morning.tower.karbo.feature.report.ui.state.ReportStatus
import pl.morning.tower.karbo.feature.report.ui.state.SendButtonState
import pl.morning.tower.karbo.feature.report.ui.state.SlotTagTone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MissingProductStateFactoryTest {

    private val factory = MissingProductStateFactory(
        resources = MissingProductResources(),
        captureStateFactory = PhotoCaptureStateFactory(PhotoCaptureResources()),
    )

    @Test
    fun `when the form is empty then it shows three empty slots in order with their copy and tags`() {
        assertEquals(
            listOf(
                PhotoSlotState(
                    slot = PhotoSlot.FRONT,
                    label = UiText.Res(R.string.report_slot_front),
                    note = UiText.Res(R.string.report_slot_front_note),
                    tag = UiText.Res(R.string.report_slot_required),
                    tagTone = SlotTagTone.REQUIRED,
                    photoUri = null,
                    actionLabel = UiText.Res(R.string.report_take_photo),
                ),
                PhotoSlotState(
                    slot = PhotoSlot.NUTRITION_TABLE,
                    label = UiText.Res(R.string.report_slot_nutrition),
                    note = UiText.Res(R.string.report_slot_nutrition_note),
                    tag = UiText.Res(R.string.report_slot_required),
                    tagTone = SlotTagTone.REQUIRED,
                    photoUri = null,
                    actionLabel = UiText.Res(R.string.report_take_photo),
                ),
                PhotoSlotState(
                    slot = PhotoSlot.INGREDIENTS,
                    label = UiText.Res(R.string.report_slot_ingredients),
                    note = UiText.Res(R.string.report_slot_ingredients_note),
                    tag = UiText.Res(R.string.report_slot_recommended),
                    tagTone = SlotTagTone.RECOMMENDED,
                    photoUri = null,
                    actionLabel = UiText.Res(R.string.report_take_photo),
                ),
            ),
            form(prototype()).slots,
        )
    }

    @Test
    fun `when a slot has a photo then it shows the uri and offers a retake`() {
        val slot = form(prototype(slotPhotos = mapOf(PhotoSlot.FRONT to ReportPhoto(PHOTO_1)))).slots.first()

        assertEquals(PHOTO_1, slot.photoUri)
        assertEquals(UiText.Res(R.string.report_retake), slot.actionLabel)
    }

    @Test
    fun `when the form is shown then the barcode chip names the barcode`() {
        assertEquals(
            UiText.Res(R.string.report_missing_barcode, UiArg.Text(BARCODE)),
            form(prototype()).barcode,
        )
    }

    @Test
    fun `when a required photo is missing then send is disabled and asks for the front and nutrition photos`() {
        val missingRequired = listOf(
            emptyMap(),
            mapOf(PhotoSlot.FRONT to ReportPhoto(PHOTO_1)),
            mapOf(PhotoSlot.NUTRITION_TABLE to ReportPhoto(PHOTO_2)),
            mapOf(PhotoSlot.FRONT to ReportPhoto(PHOTO_1), PhotoSlot.INGREDIENTS to ReportPhoto(PHOTO_3)),
        )

        missingRequired.forEach { slotPhotos ->
            assertEquals(
                SendButtonState(
                    label = UiText.Res(R.string.report_add_required_photos),
                    isEnabled = false,
                    isLoading = false,
                ),
                form(prototype(slotPhotos = slotPhotos)).sendButton,
            )
        }
    }

    @Test
    fun `when the front and nutrition photos are taken then send is enabled with or without ingredients`() {
        val required = mapOf(PhotoSlot.FRONT to ReportPhoto(PHOTO_1), PhotoSlot.NUTRITION_TABLE to ReportPhoto(PHOTO_2))

        listOf(required, required + (PhotoSlot.INGREDIENTS to ReportPhoto(PHOTO_3))).forEach { slotPhotos ->
            assertEquals(
                SendButtonState(label = UiText.Res(R.string.report_send_photos), isEnabled = true, isLoading = false),
                form(prototype(slotPhotos = slotPhotos)).sendButton,
            )
        }
    }

    @Test
    fun `when sending then the button shows progress and the slots are locked`() {
        val form = form(prototype(slotPhotos = READY, status = ReportStatus.SENDING))

        assertEquals(
            SendButtonState(label = UiText.Res(R.string.report_send_photos), isEnabled = true, isLoading = true),
            form.sendButton,
        )
        assertFalse(form.isEditable)
    }

    @Test
    fun `when editing then the slots are editable`() {
        assertTrue(form(prototype()).isEditable)
    }

    @Test
    fun `when sent or failed then the matching content is shown`() {
        assertEquals(MissingProductContentState.Sent, state(prototype(status = ReportStatus.SENT)).content)
        assertEquals(MissingProductContentState.Failed, state(prototype(status = ReportStatus.FAILED)).content)
    }

    @Test
    fun `when the capture is closed then there is no capture state`() {
        assertNull(state(prototype(), capture = PhotoCapturePrototype(isOpen = false)).photoCapture)
    }

    @Test
    fun `when not editing then there is no capture state even if it is open`() {
        listOf(ReportStatus.SENDING, ReportStatus.SENT, ReportStatus.FAILED).forEach { status ->
            assertNull(state(prototype(slotPhotos = READY, status = status), capture = OPEN).photoCapture)
        }
    }

    @Test
    fun `when the capture is open then the counter and copy follow the capture slot`() {
        val expected = mapOf(
            PhotoSlot.FRONT to Triple(1, R.string.report_slot_front, R.string.report_capture_frame_front),
            PhotoSlot.NUTRITION_TABLE to
                Triple(2, R.string.report_slot_nutrition, R.string.report_capture_frame_nutrition),
            PhotoSlot.INGREDIENTS to
                Triple(3, R.string.report_slot_ingredients, R.string.report_capture_frame_ingredients),
        )

        expected.forEach { (slot, copy) ->
            val capture = assertNotNull(state(prototype(captureSlot = slot), capture = OPEN).photoCapture)
            assertEquals(
                UiText.Res(
                    R.string.report_photo_counter_named,
                    UiArg.Numeric.Count(copy.first.toLong()),
                    UiArg.Numeric.Count(3),
                    UiArg.Nested(UiText.Res(copy.second)),
                ),
                capture.counter,
            )
            assertEquals(UiText.Res(copy.third), assertIs<CaptureStepState.Camera>(capture.step).frameHint)
        }
    }

    private fun state(
        prototype: MissingProductStatePrototype,
        capture: PhotoCapturePrototype = PhotoCapturePrototype(),
    ): MissingProductState = factory.createFor(prototype = prototype, capture = capture)

    private fun form(prototype: MissingProductStatePrototype): MissingProductContentState.Form =
        assertIs<MissingProductContentState.Form>(state(prototype).content)

    private companion object {
        const val BARCODE = "5900512300320"
        const val PHOTO_1 = "file:///photos/1"
        const val PHOTO_2 = "file:///photos/2"
        const val PHOTO_3 = "file:///photos/3"

        val READY = mapOf(PhotoSlot.FRONT to ReportPhoto(PHOTO_1), PhotoSlot.NUTRITION_TABLE to ReportPhoto(PHOTO_2))
        val OPEN = PhotoCapturePrototype(isOpen = true, cameraPermission = CameraPermission.Granted)

        fun prototype(
            slotPhotos: Map<PhotoSlot, ReportPhoto> = emptyMap(),
            captureSlot: PhotoSlot = PhotoSlot.FRONT,
            status: ReportStatus = ReportStatus.EDITING,
        ) = MissingProductStatePrototype(
            barcode = Barcode(BARCODE),
            slotPhotos = slotPhotos,
            captureSlot = captureSlot,
            status = status,
        )
    }
}
