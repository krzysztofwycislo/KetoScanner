package pl.morning.tower.karbo.feature.report.ui.capture

import pl.morning.tower.karbo.core.designsystem.text.UiArg
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto
import pl.morning.tower.karbo.feature.report.ui.R
import pl.morning.tower.karbo.feature.report.ui.capture.state.CameraPermission
import pl.morning.tower.karbo.feature.report.ui.capture.state.CaptureStepState
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCapturePrototype
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCaptureRequest
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCaptureStateFactory
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoSubject
import pl.morning.tower.karbo.feature.report.ui.capture.state.TorchButtonState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhotoCaptureStateFactoryTest {

    private val factory = PhotoCaptureStateFactory(PhotoCaptureResources())

    @Test
    fun `when the capture is closed then there is no state`() {
        assertNull(
            factory.createFor(
                capture = PhotoCapturePrototype(isOpen = false, cameraPermission = CameraPermission.Granted),
                request = request(),
            ),
        )
    }

    @Test
    fun `when the permission is unchecked or requesting then the waiting step is shown`() {
        listOf(CameraPermission.Unchecked, CameraPermission.Requesting).forEach { permission ->
            assertEquals(CaptureStepState.Waiting, step(open(cameraPermission = permission)))
        }
    }

    @Test
    fun `when the permission is granted then the camera step shows the frame hint for the subject`() {
        val expected = mapOf(
            PhotoSubject.NUTRITION_TABLE to R.string.report_capture_frame_nutrition,
            PhotoSubject.LABEL to R.string.report_capture_frame_label,
        )

        expected.forEach { (subject, id) ->
            val camera = assertIs<CaptureStepState.Camera>(step(open(), request(subject = subject)))
            assertEquals(UiText.Res(id), camera.frameHint)
        }
    }

    @Test
    fun `when the device has no torch then the torch button is absent and the torch is off`() {
        val camera = assertIs<CaptureStepState.Camera>(step(open(hasTorch = false, isTorchOn = true)))

        assertNull(camera.torchButton)
        assertFalse(camera.isTorchOn)
    }

    @Test
    fun `when the device has a torch then the button offers to turn it on or off`() {
        val off = assertIs<CaptureStepState.Camera>(step(open(hasTorch = true, isTorchOn = false)))
        val on = assertIs<CaptureStepState.Camera>(step(open(hasTorch = true, isTorchOn = true)))

        assertEquals(
            TorchButtonState(isActive = false, contentDescription = UiText.Res(R.string.report_torch_on)),
            off.torchButton,
        )
        assertFalse(off.isTorchOn)
        assertEquals(
            TorchButtonState(isActive = true, contentDescription = UiText.Res(R.string.report_torch_off)),
            on.torchButton,
        )
        assertTrue(on.isTorchOn)
    }

    @Test
    fun `when a photo is being taken then the shutter is disabled`() {
        val idle = assertIs<CaptureStepState.Camera>(step(open(isCapturing = false)))
        val capturing = assertIs<CaptureStepState.Camera>(step(open(isCapturing = true)))

        assertTrue(idle.isShutterEnabled)
        assertFalse(capturing.isShutterEnabled)
    }

    @Test
    fun `when there is a preview photo then the preview step shows the subject copy even when denied`() {
        val preview = step(
            open(cameraPermission = CameraPermission.Denied, previewPhoto = ReportPhoto(PHOTO)),
            request(subject = PhotoSubject.NUTRITION_TABLE),
        )

        assertEquals(
            CaptureStepState.Preview(
                imageUri = PHOTO,
                title = UiText.Res(R.string.report_preview_nutrition_title),
                body = UiText.Res(R.string.report_preview_nutrition_body),
            ),
            preview,
        )
    }

    @Test
    fun `when the subject is a label then the preview asks about the label`() {
        val preview = assertIs<CaptureStepState.Preview>(
            step(open(previewPhoto = ReportPhoto(PHOTO)), request(subject = PhotoSubject.LABEL)),
        )

        assertEquals(UiText.Res(R.string.report_preview_label_title), preview.title)
        assertEquals(UiText.Res(R.string.report_preview_label_body), preview.body)
    }

    @Test
    fun `when the permission needs a rationale then the blocked step asks to allow the camera`() {
        assertEquals(
            CaptureStepState.Blocked(
                title = UiText.Res(R.string.report_camera_rationale_title),
                body = UiText.Res(R.string.report_camera_rationale_body),
                primaryLabel = UiText.Res(R.string.report_allow_camera),
            ),
            step(open(cameraPermission = CameraPermission.Rationale)),
        )
    }

    @Test
    fun `when the permission is denied then the blocked step offers the settings`() {
        assertEquals(
            CaptureStepState.Blocked(
                title = UiText.Res(R.string.report_camera_blocked_title),
                body = UiText.Res(R.string.report_camera_blocked_body),
                primaryLabel = UiText.Res(R.string.report_open_settings),
            ),
            step(open(cameraPermission = CameraPermission.Denied)),
        )
    }

    @Test
    fun `when open then the counter comes from the request`() {
        val state = assertNotNull(factory.createFor(capture = open(), request = request(number = 2, total = 3)))

        assertEquals(
            UiText.Res(R.string.report_photo_counter, UiArg.Numeric.Count(2), UiArg.Numeric.Count(3)),
            state.counter,
        )
    }

    private fun step(
        capture: PhotoCapturePrototype,
        request: PhotoCaptureRequest = request(),
    ): CaptureStepState = assertNotNull(factory.createFor(capture = capture, request = request)).step

    private companion object {
        const val PHOTO = "file:///photos/preview"

        fun open(
            cameraPermission: CameraPermission = CameraPermission.Granted,
            hasTorch: Boolean = true,
            isTorchOn: Boolean = false,
            isCapturing: Boolean = false,
            previewPhoto: ReportPhoto? = null,
        ) = PhotoCapturePrototype(
            isOpen = true,
            cameraPermission = cameraPermission,
            hasTorch = hasTorch,
            isTorchOn = isTorchOn,
            isCapturing = isCapturing,
            previewPhoto = previewPhoto,
        )

        fun request(
            number: Int = 1,
            total: Int = 3,
            subject: PhotoSubject = PhotoSubject.LABEL,
        ) = PhotoCaptureRequest(number = number, total = total, subject = subject)
    }
}
