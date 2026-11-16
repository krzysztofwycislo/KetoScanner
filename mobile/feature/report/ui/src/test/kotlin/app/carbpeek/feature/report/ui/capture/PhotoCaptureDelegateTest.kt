package app.carbpeek.feature.report.ui.capture

import androidx.lifecycle.SavedStateHandle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.designsystem.component.MessageTone
import app.carbpeek.core.designsystem.event.AppEvent
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.testing.FakeErrorReporter
import app.carbpeek.feature.report.api.model.ReportPhoto
import app.carbpeek.feature.report.ui.FakeReportPhotoStore
import app.carbpeek.feature.report.ui.R
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraChecked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraPermissionResult
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCancelClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnGalleryClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPermissionButtonClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPhotoPicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureFailed
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureSaved
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnRetakeClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnShutterClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnTorchClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnUsePhotoClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.OpenAppSettings
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.OpenPhotoPicker
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.RequestCameraPermission
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureEvent.TakePicture
import app.carbpeek.feature.report.ui.capture.state.CameraPermission
import app.carbpeek.feature.report.ui.capture.state.CameraPermissionCheck
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PhotoCaptureDelegateTest {

    private val photoStore = FakeReportPhotoStore()
    private val errorReporter = FakeErrorReporter()
    private val accepted = mutableListOf<ReportPhoto>()
    private val appEventLog = mutableListOf<AppEvent>()
    private var testAppEvents: AppEvents? = null

    @Test
    fun `when opened then the state and the handle are open with the torch off`() = runTest {
        val handle = handle(KEY_TORCH to true)
        val delegate = delegate(handle)

        delegate.open()

        assertTrue(delegate.state.value.isOpen)
        assertFalse(delegate.state.value.isTorchOn)
        assertEquals(true, handle.get<Boolean>(KEY_OPEN))
        assertEquals(false, handle.get<Boolean>(KEY_TORCH))
    }

    @Test
    fun `when the camera is checked as granted then the permission is granted and the torch is stored`() = runTest {
        val handle = handle()
        val delegate = delegate(handle)
        delegate.open()

        delegate.onAction(OnCameraChecked(check = GRANTED, hasTorch = true))

        assertEquals(CameraPermission.Granted, delegate.state.value.cameraPermission)
        assertTrue(delegate.state.value.hasTorch)
        assertEquals(CameraPermission.Granted.name, handle.get<String>(KEY_CAMERA_PERMISSION))
    }

    @Test
    fun `when the camera is checked with a rationale then the rationale is shown`() = runTest {
        val delegate = delegate()
        delegate.open()

        delegate.onAction(OnCameraChecked(check = RATIONALE, hasTorch = false))

        assertEquals(CameraPermission.Rationale, delegate.state.value.cameraPermission)
    }

    @Test
    fun `when the camera is checked without grant or rationale then the permission is requested`() = runTest {
        val delegate = delegate()
        val events = collectEvents(delegate)
        delegate.open()

        delegate.onAction(OnCameraChecked(check = NOT_GRANTED, hasTorch = false))

        assertEquals(CameraPermission.Requesting, delegate.state.value.cameraPermission)
        assertEquals(listOf<PhotoCaptureEvent>(RequestCameraPermission), events)
    }

    @Test
    fun `when the camera is checked while requesting or denied then the permission stays`() = runTest {
        listOf(CameraPermission.Requesting, CameraPermission.Denied).forEach { permission ->
            val delegate = delegate(handle(KEY_CAMERA_PERMISSION to permission.name))
            val events = collectEvents(delegate)
            delegate.open()

            delegate.onAction(OnCameraChecked(check = NOT_GRANTED, hasTorch = false))
            delegate.onAction(OnCameraChecked(check = RATIONALE, hasTorch = false))

            assertEquals(permission, delegate.state.value.cameraPermission)
            assertTrue(events.isEmpty())
        }
    }

    @Test
    fun `when the camera is checked while closed then nothing changes`() = runTest {
        val delegate = delegate()
        val events = collectEvents(delegate)

        delegate.onAction(OnCameraChecked(check = NOT_GRANTED, hasTorch = true))

        assertEquals(CameraPermission.Unchecked, delegate.state.value.cameraPermission)
        assertFalse(delegate.state.value.hasTorch)
        assertTrue(events.isEmpty())
    }

    @Test
    fun `when the permission result arrives then it maps to granted, rationale or denied`() = runTest {
        val expected = mapOf(
            GRANTED to CameraPermission.Granted,
            RATIONALE to CameraPermission.Rationale,
            NOT_GRANTED to CameraPermission.Denied,
        )

        expected.forEach { (check, permission) ->
            val handle = handle()
            val delegate = delegate(handle)

            delegate.onAction(OnCameraPermissionResult(check))

            assertEquals(permission, delegate.state.value.cameraPermission)
            assertEquals(permission.name, handle.get<String>(KEY_CAMERA_PERMISSION))
        }
    }

    @Test
    fun `when the permission button is clicked on the rationale then the permission is requested`() = runTest {
        val delegate = delegate(handle(KEY_CAMERA_PERMISSION to CameraPermission.Rationale.name))
        val events = collectEvents(delegate)
        delegate.open()

        delegate.onAction(OnPermissionButtonClicked)

        assertEquals(CameraPermission.Requesting, delegate.state.value.cameraPermission)
        assertEquals(listOf<PhotoCaptureEvent>(RequestCameraPermission), events)
    }

    @Test
    fun `when the permission button is clicked while denied then the app settings open`() = runTest {
        val delegate = delegate(handle(KEY_CAMERA_PERMISSION to CameraPermission.Denied.name))
        val events = collectEvents(delegate)
        delegate.open()

        delegate.onAction(OnPermissionButtonClicked)

        assertEquals(listOf<PhotoCaptureEvent>(OpenAppSettings), events)
    }

    @Test
    fun `when the handle holds a granted permission then it is restored as unchecked`() = runTest {
        val delegate = delegate(handle(KEY_CAMERA_PERMISSION to CameraPermission.Granted.name))

        assertEquals(CameraPermission.Unchecked, delegate.state.value.cameraPermission)
    }

    @Test
    fun `when the handle holds an open capture then it is restored with its preview and torch`() = runTest {
        val delegate = delegate(handle(KEY_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO, KEY_TORCH to true))

        val capture = delegate.state.value
        assertTrue(capture.isOpen)
        assertEquals(ReportPhoto(PHOTO), capture.previewPhoto)
        assertTrue(capture.isTorchOn)
        assertEquals(listOf(ReportPhoto(PHOTO)), delegate.photos)
    }

    @Test
    fun `when the torch is clicked then it toggles and is saved`() = runTest {
        val handle = handle()
        val delegate = grantedDelegate(handle, hasTorch = true)

        delegate.onAction(OnTorchClicked)
        assertTrue(delegate.state.value.isTorchOn)
        assertEquals(true, handle.get<Boolean>(KEY_TORCH))

        delegate.onAction(OnTorchClicked)
        assertFalse(delegate.state.value.isTorchOn)
        assertEquals(false, handle.get<Boolean>(KEY_TORCH))
    }

    @Test
    fun `when the torch is clicked without a torch then nothing changes`() = runTest {
        val delegate = grantedDelegate(hasTorch = false)

        delegate.onAction(OnTorchClicked)

        assertFalse(delegate.state.value.isTorchOn)
    }

    @Test
    fun `when the shutter is clicked then a new photo file is created and taking the picture is requested`() =
        runTest {
            val delegate = grantedDelegate()
            val events = collectEvents(delegate)

            delegate.onAction(OnShutterClicked)
            delegate.onAction(OnShutterClicked)
            runCurrent()

            val created = photoStore.created.single()
            assertEquals(listOf<PhotoCaptureEvent>(TakePicture(created.uri)), events)
            assertTrue(delegate.state.value.isCapturing)
        }

    @Test
    fun `when the shutter is clicked without the permission or with a preview then nothing happens`() = runTest {
        val notGranted = delegate()
        notGranted.open()
        val withPreview = delegate(
            handle(KEY_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO),
        ).also { it.onAction(OnCameraChecked(check = GRANTED, hasTorch = false)) }

        notGranted.onAction(OnShutterClicked)
        withPreview.onAction(OnShutterClicked)
        runCurrent()

        assertTrue(photoStore.created.isEmpty())
        assertFalse(notGranted.state.value.isCapturing)
        assertFalse(withPreview.state.value.isCapturing)
    }

    @Test
    fun `when creating the photo file fails then a message is shown, reported and capturing ends`() = runTest {
        photoStore.newPhotoResult = Outcome.Failure(AppError.Storage)
        val delegate = grantedDelegate()
        val events = collectEvents(delegate)
        collectAppEvents()

        delegate.onAction(OnShutterClicked)
        runCurrent()

        assertTrue(events.isEmpty())
        assertEquals(listOf<AppEvent>(captureFailedMessage), appEventLog)
        assertEquals(AppError.Storage, errorReporter.reports.single().error)
        assertFalse(delegate.state.value.isCapturing)
    }

    @Test
    fun `when the picture is saved then it becomes the preview in the state and the handle`() = runTest {
        val handle = handle()
        val delegate = grantedDelegate(handle)
        delegate.onAction(OnShutterClicked)
        runCurrent()
        val photo = photoStore.created.single()

        delegate.onAction(OnPictureSaved(photo.uri))

        assertEquals(photo, delegate.state.value.previewPhoto)
        assertEquals(photo.uri, handle.get<String>(KEY_PREVIEW_PHOTO))
        assertFalse(delegate.state.value.isCapturing)
    }

    @Test
    fun `when a picture is saved while closed or with a preview then its file is deleted`() = runTest {
        val closed = delegate()
        val withPreview = delegate(handle(KEY_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO))

        closed.onAction(OnPictureSaved(NEW_PHOTO))
        withPreview.onAction(OnPictureSaved(NEW_PHOTO))
        runCurrent()

        assertEquals(listOf(ReportPhoto(NEW_PHOTO), ReportPhoto(NEW_PHOTO)), photoStore.deleted)
        assertNull(closed.state.value.previewPhoto)
        assertEquals(ReportPhoto(PHOTO), withPreview.state.value.previewPhoto)
    }

    @Test
    fun `when taking the picture fails then the file is deleted, a message is shown and it is reported`() = runTest {
        val delegate = grantedDelegate()
        collectAppEvents()
        delegate.onAction(OnShutterClicked)
        runCurrent()
        val photo = photoStore.created.single()
        val error = AppError.Unknown(IllegalStateException("closed"))

        delegate.onAction(OnPictureFailed(uri = photo.uri, error = error))
        runCurrent()

        assertEquals(listOf(photo), photoStore.deleted)
        assertEquals(listOf<AppEvent>(captureFailedMessage), appEventLog)
        assertEquals(error, errorReporter.reports.single().error)
        assertFalse(delegate.state.value.isCapturing)
        assertNull(delegate.state.value.previewPhoto)
    }

    @Test
    fun `when the gallery is clicked then the photo picker opens`() = runTest {
        val delegate = delegate()
        val events = collectEvents(delegate)
        delegate.open()

        delegate.onAction(OnGalleryClicked)

        assertEquals(listOf<PhotoCaptureEvent>(OpenPhotoPicker), events)
    }

    @Test
    fun `when the gallery is clicked while a picture is being taken then nothing happens`() = runTest {
        val delegate = grantedDelegate()
        val events = collectEvents(delegate)
        delegate.onAction(OnShutterClicked)
        runCurrent()

        delegate.onAction(OnGalleryClicked)

        assertEquals(listOf<PhotoCaptureEvent>(TakePicture(photoStore.created.single().uri)), events)
    }

    @Test
    fun `when a photo is picked then it is imported and shown as the preview`() = runTest {
        val handle = handle()
        val delegate = delegate(handle)
        delegate.open()

        delegate.onAction(OnPhotoPicked(SOURCE))
        runCurrent()

        assertEquals(listOf(SOURCE), photoStore.imported)
        assertEquals(ReportPhoto(IMPORTED_PHOTO), delegate.state.value.previewPhoto)
        assertEquals(IMPORTED_PHOTO, handle.get<String>(KEY_PREVIEW_PHOTO))
    }

    @Test
    fun `when importing a picked photo fails then a message is shown and it is reported`() = runTest {
        photoStore.importResult = Outcome.Failure(AppError.Storage)
        val delegate = delegate()
        collectAppEvents()
        delegate.open()

        delegate.onAction(OnPhotoPicked(SOURCE))
        runCurrent()

        assertEquals(
            listOf<AppEvent>(
                AppEvent.ShowMessage(text = UiText.Res(R.string.report_photo_import_failed), tone = MessageTone.Error),
            ),
            appEventLog,
        )
        assertEquals(AppError.Storage, errorReporter.reports.single().error)
        assertNull(delegate.state.value.previewPhoto)
    }

    @Test
    fun `when use photo is clicked then the photo is accepted, the capture closes and nothing is deleted`() =
        runTest {
            val handle = handle(KEY_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO, KEY_TORCH to true)
            val delegate = delegate(handle)

            delegate.onAction(OnUsePhotoClicked)
            runCurrent()

            assertEquals(listOf(ReportPhoto(PHOTO)), accepted)
            assertFalse(delegate.state.value.isOpen)
            assertNull(delegate.state.value.previewPhoto)
            assertFalse(delegate.state.value.isTorchOn)
            assertEquals(false, handle.get<Boolean>(KEY_OPEN))
            assertNull(handle.get<String>(KEY_PREVIEW_PHOTO))
            assertTrue(photoStore.deleted.isEmpty())
        }

    @Test
    fun `when retake is clicked then the preview is deleted and the camera step facts remain`() = runTest {
        val handle = handle(KEY_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO)
        val delegate = delegate(handle)
        delegate.onAction(OnCameraChecked(check = GRANTED, hasTorch = true))

        delegate.onAction(OnRetakeClicked)
        runCurrent()

        assertEquals(listOf(ReportPhoto(PHOTO)), photoStore.deleted)
        assertNull(delegate.state.value.previewPhoto)
        assertNull(handle.get<String>(KEY_PREVIEW_PHOTO))
        assertTrue(delegate.state.value.isOpen)
        assertEquals(CameraPermission.Granted, delegate.state.value.cameraPermission)
    }

    @Test
    fun `when cancel is clicked then the preview is deleted and the capture closes`() = runTest {
        val handle = handle(KEY_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO)
        val delegate = delegate(handle)

        delegate.onAction(OnCancelClicked)
        runCurrent()

        assertEquals(listOf(ReportPhoto(PHOTO)), photoStore.deleted)
        assertFalse(delegate.state.value.isOpen)
        assertNull(delegate.state.value.previewPhoto)
        assertEquals(false, handle.get<Boolean>(KEY_OPEN))
        assertNull(handle.get<String>(KEY_PREVIEW_PHOTO))
    }

    @Test
    fun `when cancel is clicked while a picture is being taken then the capture stays open`() = runTest {
        val delegate = grantedDelegate()
        delegate.onAction(OnShutterClicked)
        runCurrent()

        delegate.onAction(OnCancelClicked)

        assertTrue(delegate.state.value.isOpen)
        assertTrue(delegate.state.value.isCapturing)
    }

    @Test
    fun `when keep only does not contain the preview then the preview is dropped`() = runTest {
        val handle = handle(KEY_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO)
        val delegate = delegate(handle)

        delegate.keepOnly(listOf(ReportPhoto(PHOTO)))
        assertEquals(ReportPhoto(PHOTO), delegate.state.value.previewPhoto)

        delegate.keepOnly(emptyList())
        assertNull(delegate.state.value.previewPhoto)
        assertNull(handle.get<String>(KEY_PREVIEW_PHOTO))
    }

    @Test
    fun `when discarded then the preview is deleted`() = runTest {
        val delegate = delegate(handle(KEY_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO))

        delegate.discard()
        runCurrent()

        assertEquals(listOf(ReportPhoto(PHOTO)), photoStore.deleted)
    }

    private fun TestScope.delegate(handle: SavedStateHandle = handle()) = PhotoCaptureDelegate(
        savedStateHandle = handle,
        photoStore = photoStore,
        appEvents = appEvents(),
        resources = PhotoCaptureResources(),
        errorReporter = errorReporter,
        scope = this,
        applicationScope = this,
        onPhotoAccepted = { accepted += it },
    )

    private fun TestScope.grantedDelegate(
        handle: SavedStateHandle = handle(),
        hasTorch: Boolean = true,
    ): PhotoCaptureDelegate = delegate(handle).apply {
        open()
        onAction(OnCameraChecked(check = GRANTED, hasTorch = hasTorch))
    }

    private fun TestScope.collectEvents(delegate: PhotoCaptureDelegate): List<PhotoCaptureEvent> {
        val events = mutableListOf<PhotoCaptureEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { delegate.events.toList(events) }
        return events
    }

    private fun TestScope.appEvents(): AppEvents =
        testAppEvents ?: AppEvents(backgroundScope).also { testAppEvents = it }

    private fun TestScope.collectAppEvents() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { appEvents().events.toList(appEventLog) }
    }

    private companion object {
        const val KEY_OPEN = "captureOpen"
        const val KEY_PREVIEW_PHOTO = "previewPhoto"
        const val KEY_CAMERA_PERMISSION = "cameraPermission"
        const val KEY_TORCH = "torch"
        const val SOURCE = "content://media/picker/0/42"
        const val PHOTO = "file:///photos/preview"
        const val NEW_PHOTO = "file:///photos/new"
        const val IMPORTED_PHOTO = "file:///photos/1"

        val GRANTED = CameraPermissionCheck(isGranted = true, shouldShowRationale = false)
        val RATIONALE = CameraPermissionCheck(isGranted = false, shouldShowRationale = true)
        val NOT_GRANTED = CameraPermissionCheck(isGranted = false, shouldShowRationale = false)

        val captureFailedMessage = AppEvent.ShowMessage(
            text = UiText.Res(R.string.report_photo_capture_failed),
            tone = MessageTone.Error,
        )

        fun handle(vararg extra: Pair<String, Any>) = SavedStateHandle(mapOf(*extra))
    }
}
