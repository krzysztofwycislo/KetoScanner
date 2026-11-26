package app.carbpeek.feature.report.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.AppErrorException
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.designsystem.event.AppEvent
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.core.testing.FakeErrorReporter
import app.carbpeek.feature.analysis.api.AnalyzeProduct
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.feature.report.api.model.IssueReason
import app.carbpeek.feature.report.api.model.IssueReport
import app.carbpeek.feature.report.api.model.ReportPhoto
import app.carbpeek.feature.report.ui.capture.PhotoCaptureResources
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnCameraChecked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnPictureSaved
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnShutterClicked
import app.carbpeek.feature.report.ui.capture.event.PhotoCaptureAction.OnUsePhotoClicked
import app.carbpeek.feature.report.ui.capture.state.CameraPermissionCheck
import app.carbpeek.feature.report.ui.capture.state.CaptureStepState
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureStateFactory
import app.carbpeek.feature.report.ui.event.IssueReportAction
import app.carbpeek.feature.report.ui.event.IssueReportAction.OnCapture
import app.carbpeek.feature.report.ui.event.ReportEvent
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation.NavigateBack
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation.NavigateToHome
import app.carbpeek.feature.report.ui.event.ReportEvent.ReportNavigation.NavigateToScan
import app.carbpeek.feature.report.ui.state.IssueReportContentState
import app.carbpeek.feature.report.ui.state.IssueReportStateFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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

class IssueReportViewModelTest {

    private val analyzeProduct = FakeAnalyzeProduct()
    private val dietPreferences = FakeDietPreferences()
    private val reports = FakeReportRepository()
    private val photoStore = FakeReportPhotoStore()
    private val errorReporter = FakeErrorReporter()
    private val appEventLog = mutableListOf<AppEvent>()
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
    fun `when created then it analyses the route barcode and shows the product`() = runTest {
        val viewModel = createViewModel()

        assertEquals(listOf(BARCODE), analyzeProduct.requestedBarcodes)
        assertEquals(UiText.Raw(PRODUCT_NAME), form(viewModel).product.name)
    }

    @Test
    fun `when a reason is clicked twice then it is selected then cleared and the handle follows`() = runTest {
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.NUTRITION))
        assertEquals(IssueReason.NUTRITION, form(viewModel).reasons.single { it.isSelected }.reason)
        assertEquals(IssueReason.NUTRITION.name, handle.get<String>(KEY_REASON))

        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.NUTRITION))
        assertTrue(form(viewModel).reasons.none { it.isSelected })
        assertNull(handle.get<String>(KEY_REASON))
    }

    @Test
    fun `when details change then they are capped at 1000 characters and written to the handle`() = runTest {
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(IssueReportAction.OnDetailsChanged("a".repeat(1_200)))

        assertEquals("a".repeat(1_000), form(viewModel).details)
        assertEquals("a".repeat(1_000), handle.get<String>(KEY_DETAILS))
    }

    @Test
    fun `when an expected rate is clicked twice then it is selected then cleared and the handle follows`() =
        runTest {
            val handle = handle()
            val viewModel = createViewModel(handle)
            viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.RATING))

            viewModel.onAction(IssueReportAction.OnExpectedRateClicked(DietRate.GOOD))
            assertEquals(DietRate.GOOD, form(viewModel).expectation?.options?.single { it.isSelected }?.rate)
            assertEquals(DietRate.GOOD.name, handle.get<String>(KEY_EXPECTED_RATE))

            viewModel.onAction(IssueReportAction.OnExpectedRateClicked(DietRate.GOOD))
            assertEquals(true, form(viewModel).expectation?.options?.none { it.isSelected })
            assertNull(handle.get<String>(KEY_EXPECTED_RATE))
        }

    @Test
    fun `when the handle holds a draft then the form shows it`() = runTest {
        val viewModel = createViewModel(
            handle(
                KEY_REASON to IssueReason.RATING.name,
                KEY_DETAILS to "Plain milk",
                KEY_EXPECTED_RATE to DietRate.NEUTRAL.name,
            ),
        )

        val form = form(viewModel)
        assertEquals(IssueReason.RATING, form.reasons.single { it.isSelected }.reason)
        assertEquals("Plain milk", form.details)
        assertEquals(DietRate.NEUTRAL, form.expectation?.options?.single { it.isSelected }?.rate)
    }

    @Test
    fun `when the handle holds a sending status then the editable form is shown`() = runTest {
        val viewModel = createViewModel(handle(KEY_REASON to IssueReason.OTHER.name, KEY_STATUS to "SENDING"))

        val form = form(viewModel)
        assertTrue(form.isEditable)
        assertFalse(form.sendButton.isLoading)
    }

    @Test
    fun `when the handle holds a sent status then sent is shown`() = runTest {
        val viewModel = createViewModel(handle(KEY_STATUS to "SENT"))

        assertEquals(IssueReportContentState.Sent, viewModel.state.value.content)
    }

    @Test
    fun `when send is clicked then keyboard hides, sending is shown and repeat taps and inputs are ignored`() =
        runTest {
            val gate = reports.gate()
            val handle = handle()
            val viewModel = createViewModel(handle)
            collectAppEvents()
            viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.OTHER))
            viewModel.onAction(IssueReportAction.OnDetailsChanged("Wrong photo"))

            viewModel.onAction(IssueReportAction.OnSendClicked)
            viewModel.onAction(IssueReportAction.OnSendClicked)
            viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.PRODUCT))
            viewModel.onAction(IssueReportAction.OnDetailsChanged("Changed"))

            val form = form(viewModel)
            assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEventLog)
            assertFalse(form.isEditable)
            assertTrue(form.sendButton.isLoading)
            assertEquals(IssueReason.OTHER, form.reasons.single { it.isSelected }.reason)
            assertEquals("Wrong photo", form.details)
            assertEquals("SENDING", handle.get<String>(KEY_STATUS))
            assertEquals(1, reports.submittedIssues.size)

            gate.complete(Unit)
            assertEquals(IssueReportContentState.Sent, viewModel.state.value.content)
        }

    @Test
    fun `when send is clicked without a reason then nothing is sent`() = runTest {
        val viewModel = createViewModel()
        collectAppEvents()

        viewModel.onAction(IssueReportAction.OnSendClicked)

        assertTrue(reports.submittedIssues.isEmpty())
        assertTrue(appEventLog.isEmpty())
    }

    @Test
    fun `when sending succeeds then sent is shown and the report has trimmed details, diet mode and expected rate`() =
        runTest {
            dietPreferences.preferences.value = DietPreferences(maxDailyCarbsAmount = 80, dietMode = DietMode.LOW_CARB)
            val handle = handle()
            val viewModel = createViewModel(handle)

            viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.RATING))
            viewModel.onAction(IssueReportAction.OnExpectedRateClicked(DietRate.NEUTRAL))
            viewModel.onAction(IssueReportAction.OnDetailsChanged("  Plain milk  "))
            viewModel.onAction(IssueReportAction.OnSendClicked)

            assertEquals(IssueReportContentState.Sent, viewModel.state.value.content)
            assertEquals("SENT", handle.get<String>(KEY_STATUS))
            assertEquals(
                IssueReport(
                    barcode = Barcode(BARCODE),
                    reason = IssueReason.RATING,
                    details = "Plain milk",
                    expectedRate = DietRate.NEUTRAL,
                    dietMode = DietMode.LOW_CARB,
                    photos = emptyList(),
                ),
                reports.submittedIssues.single(),
            )
        }

    @Test
    fun `when there is no diet then the report has no diet mode and no expected rate`() = runTest {
        dietPreferences.preferences.value = DietPreferences.NoDiet
        val viewModel = createViewModel(
            handle(
                KEY_REASON to IssueReason.RATING.name,
                KEY_EXPECTED_RATE to DietRate.GOOD.name,
            ),
        )

        viewModel.onAction(IssueReportAction.OnSendClicked)
        assertTrue(reports.submittedIssues.isEmpty())
        assertFalse(form(viewModel).sendButton.isEnabled)
        assertTrue(form(viewModel).reasons.none { it.isSelected })

        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.NUTRITION))
        viewModel.onAction(IssueReportAction.OnSendClicked)

        assertEquals(
            IssueReport(
                barcode = Barcode(BARCODE),
                reason = IssueReason.NUTRITION,
                details = "",
                expectedRate = null,
                dietMode = null,
                photos = emptyList(),
            ),
            reports.submittedIssues.single(),
        )
    }

    @Test
    fun `when the preferences load late then the form appears with them and never offers the rating without a diet`() =
        runTest {
            dietPreferences.preferences.value = null
            val viewModel = createViewModel(handle(KEY_REASON to IssueReason.NUTRITION.name))

            assertEquals(IssueReportContentState.InitialLoad, viewModel.state.value.content)
            viewModel.onAction(IssueReportAction.OnSendClicked)
            assertTrue(reports.submittedIssues.isEmpty())

            dietPreferences.preferences.value = DietPreferences.NoDiet

            assertTrue(form(viewModel).reasons.none { it.reason == IssueReason.RATING })
            assertEquals(IssueReason.NUTRITION, form(viewModel).reasons.single { it.isSelected }.reason)
        }

    @Test
    fun `when loading the preferences fails then it is reported and the form shows with the default diet`() = runTest {
        dietPreferences.failure = AppError.Storage

        val viewModel = createViewModel()

        assertEquals(IssueReason.entries.toList(), form(viewModel).reasons.map { it.reason })
        assertEquals("Observing diet preferences failed", errorReporter.reports.single().message)
    }

    @Test
    fun `when the reason changes away from rating then the expected rate is not sent`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.RATING))
        viewModel.onAction(IssueReportAction.OnExpectedRateClicked(DietRate.GOOD))
        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.NUTRITION))
        viewModel.onAction(IssueReportAction.OnSendClicked)

        val report = reports.submittedIssues.single()
        assertEquals(IssueReason.NUTRITION, report.reason)
        assertNull(report.expectedRate)
        assertEquals(DietMode.KETO, report.dietMode)
    }

    @Test
    fun `when sending fails then failed is shown and the error is reported`() = runTest {
        reports.result = Outcome.Failure(AppError.Parsing)
        val handle = handle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.OTHER))
        viewModel.onAction(IssueReportAction.OnSendClicked)

        assertEquals(IssueReportContentState.Failed, viewModel.state.value.content)
        assertEquals("FAILED", handle.get<String>(KEY_STATUS))
        assertEquals(AppError.Parsing, errorReporter.reports.single().error)
    }

    @Test
    fun `when sending fails without network then failed is shown and the error is passed to the reporter`() = runTest {
        reports.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()

        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.OTHER))
        viewModel.onAction(IssueReportAction.OnSendClicked)

        assertEquals(IssueReportContentState.Failed, viewModel.state.value.content)
        assertEquals(AppError.Network, errorReporter.reports.single().error)
    }

    @Test
    fun `when try again is clicked after a failure then an equal report is sent again`() = runTest {
        reports.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()
        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.PRODUCT))
        viewModel.onAction(IssueReportAction.OnDetailsChanged("Wrong brand"))
        viewModel.onAction(IssueReportAction.OnSendClicked)

        reports.result = Outcome.Success(Unit)
        viewModel.onAction(IssueReportAction.OnTryAgainClicked)

        assertEquals(2, reports.submittedIssues.size)
        assertEquals(reports.submittedIssues[0], reports.submittedIssues[1])
        assertEquals(IssueReportContentState.Sent, viewModel.state.value.content)
    }

    @Test
    fun `when back to the report is clicked after a failure then the form keeps the draft`() = runTest {
        reports.result = Outcome.Failure(AppError.Network)
        val handle = handle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.PRODUCT))
        viewModel.onAction(IssueReportAction.OnDetailsChanged("Wrong brand"))
        viewModel.onAction(IssueReportAction.OnSendClicked)

        viewModel.onAction(IssueReportAction.OnBackToReportClicked)

        val form = form(viewModel)
        assertTrue(form.isEditable)
        assertEquals(IssueReason.PRODUCT, form.reasons.single { it.isSelected }.reason)
        assertEquals("Wrong brand", form.details)
        assertEquals("EDITING", handle.get<String>(KEY_STATUS))
    }

    @Test
    fun `when try again is clicked while editing then nothing is sent`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.OTHER))

        viewModel.onAction(IssueReportAction.OnTryAgainClicked)

        assertTrue(reports.submittedIssues.isEmpty())
        assertTrue(form(viewModel).isEditable)
    }

    @Test
    fun `when close is clicked then navigate back is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(IssueReportAction.OnCloseClicked)

        assertEquals(NavigateBack, viewModel.events.first())
    }

    @Test
    fun `when home is clicked then navigate to home is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(IssueReportAction.OnHomeClicked)

        assertEquals(NavigateToHome, viewModel.events.first())
    }

    @Test
    fun `when scan next is clicked then navigate to scan is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(IssueReportAction.OnScanNextClicked)

        assertEquals(NavigateToScan, viewModel.events.first())
    }

    @Test
    fun `when add photo is clicked then the keyboard hides and the capture opens`() = runTest {
        val handle = handle()
        val viewModel = createViewModel(handle)
        collectAppEvents()
        val screenEvents = collectScreenEvents(viewModel)

        viewModel.onAction(IssueReportAction.OnAddPhotoClicked)

        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEventLog)
        assertTrue(screenEvents.isEmpty())
        assertEquals(CaptureStepState.Waiting, assertNotNull(viewModel.state.value.photoCapture).step)
        assertEquals(true, handle.get<Boolean>(KEY_CAPTURE_OPEN))
    }

    @Test
    fun `when add photo is clicked for the rating reason then nothing happens`() = runTest {
        val viewModel = createViewModel(handle(KEY_REASON to IssueReason.RATING.name))

        assertAddPhotoIgnored(viewModel)
    }

    @Test
    fun `when add photo is clicked with three photos then nothing happens`() = runTest {
        val viewModel = createViewModel(handle(KEY_PHOTOS to uris(PHOTO_1, PHOTO_2, PHOTO_3)))

        assertAddPhotoIgnored(viewModel)
    }

    @Test
    fun `when add photo is clicked while sending then nothing happens`() = runTest {
        reports.gate()
        val viewModel = createViewModel()
        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.OTHER))
        viewModel.onAction(IssueReportAction.OnSendClicked)

        assertAddPhotoIgnored(viewModel)
    }

    @Test
    fun `when add photo is clicked while the capture is open then nothing happens`() = runTest {
        val viewModel = createViewModel(handle(KEY_CAPTURE_OPEN to true))

        assertAddPhotoIgnored(viewModel)
    }

    @Test
    fun `when a taken picture is used then it is attached and written to the handle`() = runTest {
        val handle = handle(KEY_PHOTOS to uris(PHOTO_1))
        val viewModel = createViewModel(handle)
        viewModel.onAction(IssueReportAction.OnAddPhotoClicked)
        viewModel.onAction(OnCapture(OnCameraChecked(check = GRANTED, hasTorch = false)))
        viewModel.onAction(OnCapture(OnShutterClicked))
        runCurrent()
        val taken = photoStore.created.single()

        viewModel.onAction(OnCapture(OnPictureSaved(taken.uri)))
        viewModel.onAction(OnCapture(OnUsePhotoClicked))

        assertEquals(listOf(PHOTO_1, taken.uri), form(viewModel).photos?.items?.map { it.uri })
        assertEquals(uris(PHOTO_1, taken.uri), handle.get<ArrayList<String>>(KEY_PHOTOS))
        assertNull(viewModel.state.value.photoCapture)
        assertNull(handle.get<String>(KEY_PREVIEW_PHOTO))
    }

    @Test
    fun `when a photo is used with three photos attached then it is deleted instead`() = runTest {
        val handle = handle(
            KEY_PHOTOS to uris(PHOTO_1, PHOTO_2, PHOTO_3),
            KEY_CAPTURE_OPEN to true,
            KEY_PREVIEW_PHOTO to PHOTO_4,
        )
        val viewModel = createViewModel(handle)

        viewModel.onAction(OnCapture(OnUsePhotoClicked))
        runCurrent()

        assertEquals(listOf(PHOTO_1, PHOTO_2, PHOTO_3), form(viewModel).photos?.items?.map { it.uri })
        assertEquals(listOf(ReportPhoto(PHOTO_4)), photoStore.deleted)
    }

    @Test
    fun `when a photo is removed then it is dropped from the draft and its file is deleted`() = runTest {
        val handle = handle(KEY_PHOTOS to uris(PHOTO_1, PHOTO_2))
        val viewModel = createViewModel(handle)

        viewModel.onAction(IssueReportAction.OnRemovePhotoClicked(PHOTO_1))
        runCurrent()

        assertEquals(listOf(PHOTO_2), form(viewModel).photos?.items?.map { it.uri })
        assertEquals(uris(PHOTO_2), handle.get<ArrayList<String>>(KEY_PHOTOS))
        assertEquals(listOf(ReportPhoto(PHOTO_1)), photoStore.deleted)
    }

    @Test
    fun `when sending for nutrition succeeds then the photos are sent, deleted and cleared from the handle`() =
        runTest {
            val handle = handle(KEY_REASON to IssueReason.NUTRITION.name, KEY_PHOTOS to uris(PHOTO_1, PHOTO_2))
            val viewModel = createViewModel(handle)

            viewModel.onAction(IssueReportAction.OnSendClicked)
            runCurrent()

            assertEquals(listOf(ReportPhoto(PHOTO_1), ReportPhoto(PHOTO_2)), reports.submittedIssues.single().photos)
            assertEquals(listOf(ReportPhoto(PHOTO_1), ReportPhoto(PHOTO_2)), photoStore.deleted)
            assertEquals(arrayListOf<String>(), handle.get<ArrayList<String>>(KEY_PHOTOS))
        }

    @Test
    fun `when sending for rating then no photos are sent`() = runTest {
        val viewModel = createViewModel(
            handle(KEY_REASON to IssueReason.RATING.name, KEY_PHOTOS to uris(PHOTO_1)),
        )

        viewModel.onAction(IssueReportAction.OnSendClicked)

        assertEquals(emptyList(), reports.submittedIssues.single().photos)
    }

    @Test
    fun `when sending fails then the photos are kept and try again sends them again`() = runTest {
        reports.result = Outcome.Failure(AppError.Network)
        val handle = handle(KEY_REASON to IssueReason.PRODUCT.name, KEY_PHOTOS to uris(PHOTO_1))
        val viewModel = createViewModel(handle)

        viewModel.onAction(IssueReportAction.OnSendClicked)
        runCurrent()

        assertTrue(photoStore.deleted.isEmpty())
        assertEquals(uris(PHOTO_1), handle.get<ArrayList<String>>(KEY_PHOTOS))

        reports.result = Outcome.Success(Unit)
        viewModel.onAction(IssueReportAction.OnTryAgainClicked)

        assertEquals(listOf(ReportPhoto(PHOTO_1)), reports.submittedIssues[1].photos)
    }

    @Test
    fun `when started then only the restored photos that still exist are kept`() = runTest {
        photoStore.existing = setOf(ReportPhoto(PHOTO_2))
        val handle = handle(
            KEY_PHOTOS to uris(PHOTO_1, PHOTO_2),
            KEY_CAPTURE_OPEN to true,
            KEY_PREVIEW_PHOTO to PHOTO_3,
        )
        val viewModel = createViewModel(handle)

        assertEquals(
            listOf(listOf(ReportPhoto(PHOTO_1), ReportPhoto(PHOTO_2), ReportPhoto(PHOTO_3))),
            photoStore.retained,
        )
        assertEquals(listOf(PHOTO_2), form(viewModel).photos?.items?.map { it.uri })
        assertEquals(uris(PHOTO_2), handle.get<ArrayList<String>>(KEY_PHOTOS))
        assertEquals(CaptureStepState.Waiting, assertNotNull(viewModel.state.value.photoCapture).step)
        assertNull(handle.get<String>(KEY_PREVIEW_PHOTO))
    }

    @Test
    fun `when started with a restored preview that still exists then it is shown again`() = runTest {
        val viewModel = createViewModel(handle(KEY_CAPTURE_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO_3))

        val preview = assertIs<CaptureStepState.Preview>(assertNotNull(viewModel.state.value.photoCapture).step)
        assertEquals(PHOTO_3, preview.imageUri)
    }

    @Test
    fun `when cleaning up at start fails then the draft is kept and the error is reported`() = runTest {
        photoStore.retainResult = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel(handle(KEY_PHOTOS to uris(PHOTO_1)))

        assertEquals(listOf(PHOTO_1), form(viewModel).photos?.items?.map { it.uri })
        assertEquals(AppError.Storage, errorReporter.reports.single().error)
    }

    @Test
    fun `when the view model is cleared then the remaining photos and the preview are deleted`() = runTest {
        val testScope = this
        val handle = handle(KEY_PHOTOS to uris(PHOTO_1), KEY_CAPTURE_OPEN to true, KEY_PREVIEW_PHOTO to PHOTO_2)
        val store = ViewModelStore()
        ViewModelProvider.create(
            store = store,
            factory = viewModelFactory { initializer { testScope.createViewModel(handle) } },
        )[IssueReportViewModel::class]

        store.clear()
        runCurrent()

        assertEquals(listOf(ReportPhoto(PHOTO_1), ReportPhoto(PHOTO_2)), photoStore.deleted)
    }

    private fun TestScope.assertAddPhotoIgnored(viewModel: IssueReportViewModel) {
        collectAppEvents()
        val screenEvents = collectScreenEvents(viewModel)
        appEventLog.clear()
        val captureBefore = viewModel.state.value.photoCapture

        viewModel.onAction(IssueReportAction.OnAddPhotoClicked)

        assertTrue(appEventLog.isEmpty())
        assertTrue(screenEvents.isEmpty())
        assertEquals(captureBefore, viewModel.state.value.photoCapture)
    }

    private fun TestScope.createViewModel(handle: SavedStateHandle = handle()): IssueReportViewModel =
        IssueReportViewModel(
            route = ReportRoute(barcode = BARCODE, kind = ReportKind.ISSUE),
            savedStateHandle = handle,
            analyzeProduct = analyzeProduct,
            dietPreferences = dietPreferences,
            reports = reports,
            photoStore = photoStore,
            appEvents = appEvents(),
            stateFactory = IssueReportStateFactory(
                resources = IssueReportResources(),
                captureStateFactory = PhotoCaptureStateFactory(PhotoCaptureResources()),
            ),
            captureResources = PhotoCaptureResources(),
            errorReporter = errorReporter,
            applicationScope = this,
        )

    private fun TestScope.collectScreenEvents(viewModel: IssueReportViewModel): List<ReportEvent> {
        val events = mutableListOf<ReportEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.toList(events) }
        return events
    }

    private fun TestScope.appEvents(): AppEvents =
        testAppEvents ?: AppEvents(backgroundScope).also { testAppEvents = it }

    private fun TestScope.collectAppEvents() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { appEvents().events.toList(appEventLog) }
    }

    private fun form(viewModel: IssueReportViewModel): IssueReportContentState.Form =
        assertIs<IssueReportContentState.Form>(viewModel.state.value.content)

    private class FakeAnalyzeProduct : AnalyzeProduct {

        val requestedBarcodes = mutableListOf<String>()

        override fun invoke(barcode: String): Flow<ProductAnalysis> {
            requestedBarcodes += barcode
            return flowOf(ProductAnalysis.InsufficientData(product))
        }
    }

    private class FakeDietPreferences : DietPreferencesRepository {

        val preferences = MutableStateFlow<DietPreferences?>(DietPreferences.Default)
        var failure: AppError? = null

        override fun observeDietPreferences(): Flow<DietPreferences> = flow {
            failure?.let { throw AppErrorException(it) }
            emitAll(preferences.filterNotNull())
        }

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = throw NotImplementedError()
    }

    private companion object {
        const val BARCODE = "5449000000996"
        const val PRODUCT_NAME = "Coca-Cola Original Taste"
        const val KEY_REASON = "reason"
        const val KEY_DETAILS = "details"
        const val KEY_EXPECTED_RATE = "expectedRate"
        const val KEY_STATUS = "status"
        const val KEY_PHOTOS = "photos"
        const val KEY_CAPTURE_OPEN = "captureOpen"
        const val KEY_PREVIEW_PHOTO = "previewPhoto"
        const val PHOTO_1 = "file:///photos/1"
        const val PHOTO_2 = "file:///photos/2"
        const val PHOTO_3 = "file:///photos/3"
        const val PHOTO_4 = "file:///photos/4"

        val GRANTED = CameraPermissionCheck(isGranted = true, shouldShowRationale = false)

        fun uris(vararg uris: String) = arrayListOf(*uris)

        val product = Product(
            name = PRODUCT_NAME,
            barcode = BARCODE,
            brand = "Coca-Cola",
            nutriments = ProductNutriments(
                energyPerServing = null,
                energyPer100g = null,
                fatsPerServing = null,
                fatsPer100g = null,
                proteinsPerServing = null,
                proteinsPer100g = null,
                carbohydratesPerServing = null,
                carbohydratesPer100g = null,
                saltPerServing = null,
                saltPer100g = null,
            ),
            imageUrl = null,
            servingAmount = null,
            category = ProductCategory.DRINKS,
        )

        fun handle(vararg extra: Pair<String, Any>) = SavedStateHandle(mapOf(*extra))
    }
}
