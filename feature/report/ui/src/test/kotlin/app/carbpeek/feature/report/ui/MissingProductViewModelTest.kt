package app.carbpeek.feature.report.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.testing.FakeErrorReporter
import app.carbpeek.feature.report.api.model.MissingProductReport
import app.carbpeek.feature.report.api.model.ReportPhoto
import app.carbpeek.feature.report.ui.capture.PhotoCaptureResources
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraChecked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCancelClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureSaved
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnShutterClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnUsePhotoClicked
import app.carbpeek.feature.report.ui.capture.state.CameraPermissionCheck
import app.carbpeek.feature.report.ui.capture.state.CaptureStepState
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureStateFactory
import app.carbpeek.feature.report.ui.event.MissingProductAction
import app.carbpeek.feature.report.ui.event.MissingProductAction.OnCapture
import app.carbpeek.feature.report.ui.event.MissingProductAction.OnSlotClicked
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation.NavigateBack
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation.NavigateToHome
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation.NavigateToScan
import app.carbpeek.feature.report.ui.state.MissingProductContentState
import app.carbpeek.feature.report.ui.state.MissingProductStateFactory
import app.carbpeek.feature.report.ui.state.PhotoSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MissingProductViewModelTest {

    private val reports = FakeReportRepository()
    private val photoStore = FakeReportPhotoStore()
    private val errorReporter = FakeErrorReporter()
    private var testAppEvents: AppEvents? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when a slot is clicked then the capture slot is saved and the capture opens`() = runTest {
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(OnSlotClicked(PhotoSlot.NUTRITION_TABLE))

        assertNotNull(viewModel.state.value.photoCapture)
        assertEquals(PhotoSlot.NUTRITION_TABLE.name, handle.get<String>(KEY_CAPTURE_SLOT))
    }

    @Test
    fun `when a slot is clicked while the capture is open then the capture slot is kept`() = runTest {
        val handle = handle(KEY_CAPTURE_SLOT to PhotoSlot.FRONT.name, KEY_CAPTURE_OPEN to true)
        val viewModel = createViewModel(handle)
        val captureBefore = viewModel.state.value.photoCapture

        viewModel.onAction(OnSlotClicked(PhotoSlot.INGREDIENTS))

        assertEquals(PhotoSlot.FRONT.name, handle.get<String>(KEY_CAPTURE_SLOT))
        assertEquals(captureBefore, viewModel.state.value.photoCapture)
    }

    @Test
    fun `when a slot is clicked while sending then nothing happens`() = runTest {
        reports.gate()
        val handle = handle(*readyPhotos())
        val viewModel = createViewModel(handle)
        viewModel.onAction(MissingProductAction.OnSendClicked)

        viewModel.onAction(OnSlotClicked(PhotoSlot.INGREDIENTS))

        assertNull(handle.get<String>(KEY_CAPTURE_SLOT))
        assertNull(viewModel.state.value.photoCapture)
    }

    @Test
    fun `when a taken photo is used then it fills the clicked slot and the handle`() = runTest {
        val handle = handle()
        val viewModel = createViewModel(handle)

        val taken = takePhoto(viewModel, PhotoSlot.NUTRITION_TABLE)

        val slots = form(viewModel).slots
        assertEquals(taken, slots.single { it.slot == PhotoSlot.NUTRITION_TABLE }.photoUri)
        assertNull(slots.single { it.slot == PhotoSlot.FRONT }.photoUri)
        assertEquals(taken, handle.get<String>(KEY_PHOTO_NUTRITION))
        assertNull(handle.get<String>(KEY_PHOTO_FRONT))
        assertNull(viewModel.state.value.photoCapture)
    }

    @Test
    fun `when a filled slot is retaken then the new photo replaces it and the old one is deleted`() = runTest {
        val handle = handle(KEY_PHOTO_FRONT to PHOTO_1)
        val viewModel = createViewModel(handle)

        val taken = takePhoto(viewModel, PhotoSlot.FRONT)
        runCurrent()

        assertEquals(taken, form(viewModel).slots.first().photoUri)
        assertEquals(taken, handle.get<String>(KEY_PHOTO_FRONT))
        assertEquals(listOf(ReportPhoto(PHOTO_1)), photoStore.deleted)
    }

    @Test
    fun `when the capture is cancelled then the slot keeps its old photo`() = runTest {
        val handle = handle(KEY_PHOTO_FRONT to PHOTO_1)
        val viewModel = createViewModel(handle)
        viewModel.onAction(OnSlotClicked(PhotoSlot.FRONT))

        viewModel.onAction(OnCapture(OnCancelClicked))
        runCurrent()

        assertNull(viewModel.state.value.photoCapture)
        assertEquals(PHOTO_1, form(viewModel).slots.first().photoUri)
        assertEquals(PHOTO_1, handle.get<String>(KEY_PHOTO_FRONT))
        assertFalse(ReportPhoto(PHOTO_1) in photoStore.deleted)
    }

    @Test
    fun `when send is clicked without the front and nutrition photos then nothing is sent`() = runTest {
        listOf(
            handle(),
            handle(KEY_PHOTO_FRONT to PHOTO_1),
            handle(KEY_PHOTO_NUTRITION to PHOTO_2, KEY_PHOTO_INGREDIENTS to PHOTO_3),
        ).forEach { handle ->
            val viewModel = createViewModel(handle)

            viewModel.onAction(MissingProductAction.OnSendClicked)

            assertIs<MissingProductContentState.Form>(viewModel.state.value.content)
        }
        assertTrue(reports.submittedMissingProducts.isEmpty())
    }

    @Test
    fun `when send is clicked then sending is shown and repeat taps are ignored`() = runTest {
        val gate = reports.gate()
        val handle = handle(*readyPhotos())
        val viewModel = createViewModel(handle)

        viewModel.onAction(MissingProductAction.OnSendClicked)
        viewModel.onAction(MissingProductAction.OnSendClicked)

        val form = form(viewModel)
        assertFalse(form.isEditable)
        assertTrue(form.sendButton.isLoading)
        assertEquals("SENDING", handle.get<String>(KEY_STATUS))
        assertEquals(1, reports.submittedMissingProducts.size)

        gate.complete(Unit)
        assertEquals(MissingProductContentState.Sent, viewModel.state.value.content)
    }

    @Test
    fun `when sending succeeds then sent is shown and the photos are deleted and cleared from the handle`() =
        runTest {
            val handle = handle(*readyPhotos())
            val viewModel = createViewModel(handle)

            viewModel.onAction(MissingProductAction.OnSendClicked)
            runCurrent()

            assertEquals(MissingProductContentState.Sent, viewModel.state.value.content)
            assertEquals(
                MissingProductReport(
                    barcode = Barcode(BARCODE),
                    front = ReportPhoto(PHOTO_1),
                    nutritionTable = ReportPhoto(PHOTO_2),
                    ingredients = null,
                ),
                reports.submittedMissingProducts.single(),
            )
            assertEquals(listOf(ReportPhoto(PHOTO_1), ReportPhoto(PHOTO_2)), photoStore.deleted)
            assertNull(handle.get<String>(KEY_PHOTO_FRONT))
            assertNull(handle.get<String>(KEY_PHOTO_NUTRITION))
            assertEquals("SENT", handle.get<String>(KEY_STATUS))
        }

    @Test
    fun `when all three photos are sent then the report has the ingredients`() = runTest {
        val viewModel = createViewModel(handle(*readyPhotos(), KEY_PHOTO_INGREDIENTS to PHOTO_3))

        viewModel.onAction(MissingProductAction.OnSendClicked)

        assertEquals(ReportPhoto(PHOTO_3), reports.submittedMissingProducts.single().ingredients)
    }

    @Test
    fun `when sending fails then failed is shown, the photos are kept and the error is reported`() = runTest {
        reports.result = Outcome.Failure(AppError.Network)
        val handle = handle(*readyPhotos())
        val viewModel = createViewModel(handle)

        viewModel.onAction(MissingProductAction.OnSendClicked)
        runCurrent()

        assertEquals(MissingProductContentState.Failed, viewModel.state.value.content)
        assertEquals(PHOTO_1, handle.get<String>(KEY_PHOTO_FRONT))
        assertTrue(photoStore.deleted.isEmpty())
        assertEquals(AppError.Network, errorReporter.reports.single().error)
        assertEquals("MissingProductViewModel", errorReporter.reports.single().tag)
    }

    @Test
    fun `when try again is clicked after a failure then the same photos are sent again`() = runTest {
        reports.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel(handle(*readyPhotos()))
        viewModel.onAction(MissingProductAction.OnSendClicked)
        reports.result = Outcome.Success(Unit)

        viewModel.onAction(MissingProductAction.OnTryAgainClicked)

        assertEquals(2, reports.submittedMissingProducts.size)
        assertEquals(reports.submittedMissingProducts[0], reports.submittedMissingProducts[1])
        assertEquals(MissingProductContentState.Sent, viewModel.state.value.content)
    }

    @Test
    fun `when try again is clicked while editing then nothing is sent`() = runTest {
        val viewModel = createViewModel(handle(*readyPhotos()))

        viewModel.onAction(MissingProductAction.OnTryAgainClicked)

        assertTrue(reports.submittedMissingProducts.isEmpty())
    }

    @Test
    fun `when back to report is clicked after a failure then the filled form is shown`() = runTest {
        reports.result = Outcome.Failure(AppError.Network)
        val handle = handle(*readyPhotos())
        val viewModel = createViewModel(handle)
        viewModel.onAction(MissingProductAction.OnSendClicked)

        viewModel.onAction(MissingProductAction.OnBackToReportClicked)

        val form = form(viewModel)
        assertTrue(form.isEditable)
        assertEquals(PHOTO_1, form.slots.first().photoUri)
        assertEquals("EDITING", handle.get<String>(KEY_STATUS))
    }

    @Test
    fun `when close is clicked then navigate back is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MissingProductAction.OnCloseClicked)

        assertEquals(NavigateBack, viewModel.events.first())
    }

    @Test
    fun `when home is clicked then navigate to home is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MissingProductAction.OnHomeClicked)

        assertEquals(NavigateToHome, viewModel.events.first())
    }

    @Test
    fun `when scan next is clicked then navigate to scan is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(MissingProductAction.OnScanNextClicked)

        assertEquals(NavigateToScan, viewModel.events.first())
    }

    @Test
    fun `when restored from the handle then the slot photos and the capture slot are shown`() = runTest {
        val viewModel = createViewModel(
            handle(
                KEY_PHOTO_FRONT to PHOTO_1,
                KEY_PHOTO_INGREDIENTS to PHOTO_3,
                KEY_CAPTURE_SLOT to PhotoSlot.INGREDIENTS.name,
                KEY_CAPTURE_OPEN to true,
            ),
        )

        assertEquals(listOf(PHOTO_1, null, PHOTO_3), form(viewModel).slots.map { it.photoUri })
        assertEquals(
            UiText.Res(
                R.string.report_photo_counter_named,
                UiArg.Numeric.Count(3),
                UiArg.Numeric.Count(3),
                UiArg.Nested(UiText.Res(R.string.report_slot_ingredients)),
            ),
            assertNotNull(viewModel.state.value.photoCapture).counter,
        )
    }

    @Test
    fun `when the handle holds a sending status then the form is shown for editing`() = runTest {
        val viewModel = createViewModel(handle(*readyPhotos(), KEY_STATUS to "SENDING"))

        assertTrue(form(viewModel).isEditable)
        assertTrue(reports.submittedMissingProducts.isEmpty())
    }

    @Test
    fun `when the handle holds a failed status then failed is shown`() = runTest {
        val viewModel = createViewModel(handle(*readyPhotos(), KEY_STATUS to "FAILED"))

        assertEquals(MissingProductContentState.Failed, viewModel.state.value.content)
    }

    @Test
    fun `when started then a missing slot photo is dropped and the preview is passed to the capture`() = runTest {
        photoStore.existing = setOf(ReportPhoto(PHOTO_2), ReportPhoto(PHOTO_3))
        val handle = handle(
            KEY_PHOTO_FRONT to PHOTO_1,
            KEY_PHOTO_NUTRITION to PHOTO_2,
            KEY_CAPTURE_OPEN to true,
            KEY_PREVIEW_PHOTO to PHOTO_3,
        )
        val viewModel = createViewModel(handle)

        assertEquals(
            listOf(listOf(ReportPhoto(PHOTO_1), ReportPhoto(PHOTO_2), ReportPhoto(PHOTO_3))),
            photoStore.retained,
        )
        assertEquals(listOf(null, PHOTO_2, null), form(viewModel).slots.map { it.photoUri })
        assertNull(handle.get<String>(KEY_PHOTO_FRONT))
        assertEquals(PHOTO_2, handle.get<String>(KEY_PHOTO_NUTRITION))
        val preview = assertIs<CaptureStepState.Preview>(assertNotNull(viewModel.state.value.photoCapture).step)
        assertEquals(PHOTO_3, preview.imageUri)
    }

    @Test
    fun `when cleaning up at start fails then the slot photos are kept and the error is reported`() = runTest {
        photoStore.retainResult = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel(handle(KEY_PHOTO_FRONT to PHOTO_1))

        assertEquals(PHOTO_1, form(viewModel).slots.first().photoUri)
        assertEquals(AppError.Storage, errorReporter.reports.single().error)
    }

    @Test
    fun `when the view model is cleared then the slot photos and the preview are deleted`() = runTest {
        val testScope = this
        val handle = handle(
            KEY_PHOTO_FRONT to PHOTO_1,
            KEY_PHOTO_INGREDIENTS to PHOTO_3,
            KEY_CAPTURE_OPEN to true,
            KEY_PREVIEW_PHOTO to PHOTO_4,
        )
        val store = ViewModelStore()
        ViewModelProvider.create(
            store = store,
            factory = viewModelFactory { initializer { testScope.createViewModel(handle) } },
        )[MissingProductViewModel::class]

        store.clear()
        runCurrent()

        assertEquals(listOf(ReportPhoto(PHOTO_1), ReportPhoto(PHOTO_3), ReportPhoto(PHOTO_4)), photoStore.deleted)
    }

    private fun TestScope.takePhoto(viewModel: MissingProductViewModel, slot: PhotoSlot): String {
        viewModel.onAction(OnSlotClicked(slot))
        viewModel.onAction(OnCapture(OnCameraChecked(check = GRANTED, hasTorch = false)))
        viewModel.onAction(OnCapture(OnShutterClicked))
        runCurrent()
        val taken = photoStore.created.last()
        viewModel.onAction(OnCapture(OnPictureSaved(taken.uri)))
        viewModel.onAction(OnCapture(OnUsePhotoClicked))
        return taken.uri
    }

    private fun TestScope.createViewModel(handle: SavedStateHandle = handle()): MissingProductViewModel =
        MissingProductViewModel(
            route = ReportRoute(barcode = BARCODE, kind = ReportKind.MISSING_PRODUCT),
            savedStateHandle = handle,
            reports = reports,
            photoStore = photoStore,
            appEvents = appEvents(),
            stateFactory = MissingProductStateFactory(
                resources = MissingProductResources(),
                captureStateFactory = PhotoCaptureStateFactory(PhotoCaptureResources()),
            ),
            captureResources = PhotoCaptureResources(),
            errorReporter = errorReporter,
            applicationScope = this,
        )

    private fun TestScope.appEvents(): AppEvents =
        testAppEvents ?: AppEvents(backgroundScope).also { testAppEvents = it }

    private fun form(viewModel: MissingProductViewModel): MissingProductContentState.Form =
        assertIs<MissingProductContentState.Form>(viewModel.state.value.content)

    private companion object {
        const val BARCODE = "5900512300320"
        const val KEY_PHOTO_FRONT = "photo_FRONT"
        const val KEY_PHOTO_NUTRITION = "photo_NUTRITION_TABLE"
        const val KEY_PHOTO_INGREDIENTS = "photo_INGREDIENTS"
        const val KEY_CAPTURE_SLOT = "captureSlot"
        const val KEY_STATUS = "status"
        const val KEY_CAPTURE_OPEN = "captureOpen"
        const val KEY_PREVIEW_PHOTO = "previewPhoto"
        const val PHOTO_1 = "file:///photos/1"
        const val PHOTO_2 = "file:///photos/2"
        const val PHOTO_3 = "file:///photos/3"
        const val PHOTO_4 = "file:///photos/4"

        val GRANTED = CameraPermissionCheck(isGranted = true, shouldShowRationale = false)

        fun readyPhotos() = arrayOf<Pair<String, Any>>(KEY_PHOTO_FRONT to PHOTO_1, KEY_PHOTO_NUTRITION to PHOTO_2)

        fun handle(vararg extra: Pair<String, Any>) = SavedStateHandle(mapOf(*extra))
    }
}
