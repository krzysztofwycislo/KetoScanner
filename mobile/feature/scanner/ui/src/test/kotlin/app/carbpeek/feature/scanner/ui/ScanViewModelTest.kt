package app.carbpeek.feature.scanner.ui

import androidx.lifecycle.SavedStateHandle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.getOrThrow
import app.carbpeek.core.common.map
import app.carbpeek.core.designsystem.component.MessageTone
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.component.ScanFrameMode
import app.carbpeek.core.designsystem.event.AppEvent
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.core.testing.FakeClock
import app.carbpeek.core.testing.FakeErrorReporter
import app.carbpeek.feature.analysis.api.AnalyzeProduct
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.history.api.AnalysisHistoryRepository
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.preferences.api.OnboardingRepository
import app.carbpeek.feature.preferences.api.ProfileSettingsRepository
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.preferences.api.model.OnboardingTip
import app.carbpeek.feature.preferences.api.model.ProfileSettings
import app.carbpeek.feature.product.api.ProductRepository
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.feature.scanner.ui.event.OpenAppSettings
import app.carbpeek.feature.scanner.ui.event.PerformHapticFeedback
import app.carbpeek.feature.scanner.ui.event.RequestCameraPermission
import app.carbpeek.feature.scanner.ui.event.ScanAction
import app.carbpeek.feature.scanner.ui.event.ScanEvent
import app.carbpeek.feature.scanner.ui.event.ScanEvent.ScanNavigation.NavigateToAddProduct
import app.carbpeek.feature.scanner.ui.event.ScanEvent.ScanNavigation.NavigateToProduct
import app.carbpeek.feature.scanner.ui.event.ScanFeedback
import app.carbpeek.feature.scanner.ui.state.CameraHintState
import app.carbpeek.feature.scanner.ui.state.CameraPermissionCheck
import app.carbpeek.feature.scanner.ui.state.CameraState
import app.carbpeek.feature.scanner.ui.state.FoundCardState
import app.carbpeek.feature.scanner.ui.state.NotFoundCardState
import app.carbpeek.feature.scanner.ui.state.ScanState
import app.carbpeek.feature.scanner.ui.state.ScanStateFactory
import app.carbpeek.feature.scanner.ui.state.TorchButtonState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ScanViewModelTest {

    private val products = FakeProductRepository()
    private val analyzeProduct = FakeAnalyzeProduct()
    private val history = FakeHistory()
    private val preferences = FakePreferences()
    private val onboarding = FakeOnboarding()
    private val clock = FakeClock(NOW)
    private val errorReporter = FakeErrorReporter()
    private val appEvents = mutableListOf<AppEvent>()
    private val screenEvents = mutableListOf<ScanEvent>()
    private val waitingPreview = preview(isPaused = true, frame = ScanFrameMode.Waiting)
    private val searchingPreview =
        preview(isPaused = true, frame = ScanFrameMode.Searching, hint = SEARCHING_HINT)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when handle has a query then it is restored instead of the initial query`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_QUERY to "123")), initialQuery = "999")

        assertEquals("123", viewModel.state.value.query)
    }

    @Test
    fun `when handle has no query then the initial query is used`() = runTest {
        val viewModel = createViewModel(initialQuery = "999")

        assertEquals("999", viewModel.state.value.query)
    }

    @Test
    fun `when query changes then state and handle follow`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ScanAction.OnQueryChanged("42"))

        assertEquals("42", viewModel.state.value.query)
        assertEquals("42", handle.get<String>(KEY_QUERY))
    }

    @Test
    fun `when started with permission granted then preview is shown`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertEquals("Granted", handle.get<String>(KEY_CAMERA_PERMISSION))
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when started with rationale then rationale panel is shown without a request`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ScanAction.OnStart(RATIONALE, hasTorch = false))

        assertEquals(rationalePanel, viewModel.state.value.camera)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when started for the first time without permission then it is requested once`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ScanAction.OnStart(NOT_GRANTED, hasTorch = false))

        assertEquals(listOf<ScanEvent>(RequestCameraPermission), screenEvents)
        assertEquals("Requesting", handle.get<String>(KEY_CAMERA_PERMISSION))
        assertEquals(CameraState.Hidden, viewModel.state.value.camera)
    }

    @Test
    fun `when restored as granted then camera stays hidden until the live check grants it`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_CAMERA_PERMISSION to "Granted")))

        assertEquals(CameraState.Hidden, viewModel.state.value.camera)

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when restored after a permanent denial then denied panel is shown without a request`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_CAMERA_PERMISSION to "Denied")))

        viewModel.onAction(ScanAction.OnStart(NOT_GRANTED, hasTorch = false))

        assertEquals(deniedPanel, viewModel.state.value.camera)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when restored while a request is in flight then it is not requested again`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_CAMERA_PERMISSION to "Requesting")))

        viewModel.onAction(ScanAction.OnStart(NOT_GRANTED, hasTorch = false))

        assertTrue(screenEvents.isEmpty())
        assertEquals(CameraState.Hidden, viewModel.state.value.camera)
    }

    @Test
    fun `when permission result arrives then it maps to granted rationale or denied and is stored`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ScanAction.OnCameraPermissionResult(GRANTED))
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertEquals("Granted", handle.get<String>(KEY_CAMERA_PERMISSION))

        viewModel.onAction(ScanAction.OnCameraPermissionResult(RATIONALE))
        assertEquals(rationalePanel, viewModel.state.value.camera)
        assertEquals("Rationale", handle.get<String>(KEY_CAMERA_PERMISSION))

        viewModel.onAction(ScanAction.OnCameraPermissionResult(NOT_GRANTED))
        assertEquals(deniedPanel, viewModel.state.value.camera)
        assertEquals("Denied", handle.get<String>(KEY_CAMERA_PERMISSION))
    }

    @Test
    fun `when denied and started again with permission granted in settings then preview is shown`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_CAMERA_PERMISSION to "Denied")))

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when permission button is clicked in rationale then permission is requested`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(RATIONALE, hasTorch = false))

        viewModel.onAction(ScanAction.OnPermissionButtonClicked)

        assertEquals(listOf<ScanEvent>(RequestCameraPermission), screenEvents)
        assertEquals(CameraState.Hidden, viewModel.state.value.camera)
    }

    @Test
    fun `when permission button is clicked in denied then app settings are opened`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_CAMERA_PERMISSION to "Denied")))

        viewModel.onAction(ScanAction.OnPermissionButtonClicked)

        assertEquals(listOf<ScanEvent>(OpenAppSettings), screenEvents)
        assertEquals(deniedPanel, viewModel.state.value.camera)
    }

    @Test
    fun `when a search is submitted then keyboard hides trimmed barcode is refreshed and the found card shows`() =
        runTest {
            analyzeProduct.emit(ratedAnalysis)
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnQueryChanged("  590  "))
            viewModel.onAction(ScanAction.OnSearchSubmitted)

            assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEvents)
            assertEquals(listOf("590"), products.refreshedBarcodes)
            assertEquals(listOf<ScanEvent>(FOUND_HAPTIC), screenEvents)
            assertEquals(ratedCard, viewModel.state.value.found)
            assertEquals(preview(isPaused = true, frame = ScanFrameMode.Found), viewModel.state.value.camera)

            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            assertEquals(preview(isPaused = true, frame = ScanFrameMode.Found), viewModel.state.value.camera)
        }

    @Test
    fun `when a blank query is submitted then nothing happens`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ScanAction.OnQueryChanged("   "))
        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertTrue(products.refreshedBarcodes.isEmpty())
        assertTrue(appEvents.isEmpty())
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when a search is pending then repeated submits and scans are ignored`() = runTest {
        val gate = products.gate()
        val viewModel = createViewModel(initialQuery = "590")
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnSearchSubmitted)
        assertFalse(viewModel.state.value.isLookUpEnabled)
        viewModel.onAction(ScanAction.OnSearchSubmitted)
        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))
        gate.complete(Unit)
        analyzeProduct.emit(ratedAnalysis)

        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertTrue(viewModel.state.value.isLookUpEnabled)
        assertEquals(ratedCard, viewModel.state.value.found)
        assertEquals(listOf<ScanEvent>(FOUND_HAPTIC), screenEvents)
    }

    @Test
    fun `when a typed search is pending and the screen starts again then camera stays paused`() = runTest {
        val gate = products.gate()
        val viewModel = createViewModel(initialQuery = "590")
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnSearchSubmitted)

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(
            preview(isPaused = true, frame = ScanFrameMode.Searching, hint = SEARCHING_HINT),
            viewModel.state.value.camera,
        )
        gate.complete(Unit)
    }

    @Test
    fun `when a scanned product is not found then the card shows it is reported and the camera stays paused`() =
        runTest {
            products.result = Outcome.Failure(AppError.NotFound)
            val handle = SavedStateHandle()
            val viewModel = createViewModel(handle)
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEvents)
            assertEquals(AppError.NotFound, errorReporter.reports.single().error)
            assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, NOT_FOUND_HAPTIC), screenEvents)
            assertTrue(viewModel.state.value.isLookUpEnabled)
            assertEquals(notFoundCard("590"), viewModel.state.value.notFound)
            assertEquals("590", handle.get<String>(KEY_NOT_FOUND_BARCODE))
            assertEquals(preview(isPaused = true, frame = ScanFrameMode.NotFound), viewModel.state.value.camera)

            advanceTimeBy(2_000)
            runCurrent()
            assertEquals(preview(isPaused = true, frame = ScanFrameMode.NotFound), viewModel.state.value.camera)

            advanceTimeBy(60_000)
            runCurrent()
            assertEquals(preview(isPaused = true, frame = ScanFrameMode.NotFound), viewModel.state.value.camera)
            assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEvents)
        }

    @Test
    fun `when scan next is clicked after not found then the card clears and the camera resumes`() = runTest {
        products.result = Outcome.Failure(AppError.NotFound)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnScanNextClicked)

        assertNull(viewModel.state.value.notFound)
        assertNull(handle.get<String>(KEY_NOT_FOUND_BARCODE))
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)

        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))

        assertEquals(listOf("590", "123"), products.refreshedBarcodes)
    }

    @Test
    fun `when add it is clicked after not found then navigates to add the product and the card stays`() = runTest {
        products.result = Outcome.Failure(AppError.NotFound)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnAddItClicked)

        assertEquals(
            listOf<ScanEvent>(DETECTED_HAPTIC, NOT_FOUND_HAPTIC, NavigateToAddProduct("590")),
            screenEvents,
        )
        assertEquals(notFoundCard("590"), viewModel.state.value.notFound)
        assertEquals("590", handle.get<String>(KEY_NOT_FOUND_BARCODE))
    }

    @Test
    fun `when add it is clicked without a not found barcode then nothing is sent`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnAddItClicked)

        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when type it is clicked after not found then the sheet opens with the barcode and the card clears`() =
        runTest {
            products.result = Outcome.Failure(AppError.NotFound)
            val handle = SavedStateHandle()
            val viewModel = createViewModel(handle)
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            viewModel.onAction(ScanAction.OnTypeItClicked)

            assertNull(viewModel.state.value.notFound)
            assertNull(handle.get<String>(KEY_NOT_FOUND_BARCODE))
            assertEquals("590", viewModel.state.value.query)
            assertEquals("590", handle.get<String>(KEY_QUERY))
            assertEquals(true, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))
            assertEquals(
                preview(isPaused = true, frame = ScanFrameMode.Idle, isTypeSheetOpen = true),
                viewModel.state.value.camera,
            )

            viewModel.onAction(ScanAction.OnTypeSheetDismissed)

            assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        }

    @Test
    fun `when the not found card is shown then scans and submits are ignored`() = runTest {
        products.result = Outcome.Failure(AppError.NotFound)
        val viewModel = createViewModel(initialQuery = "777")
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))
        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertEquals(notFoundCard("590"), viewModel.state.value.notFound)
    }

    @Test
    fun `when restored with a not found barcode then the card shows and the camera stays paused`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_NOT_FOUND_BARCODE to "590")))

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(notFoundCard("590"), viewModel.state.value.notFound)
        assertEquals(preview(isPaused = true, frame = ScanFrameMode.NotFound), viewModel.state.value.camera)
        assertTrue(products.refreshedBarcodes.isEmpty())
    }

    @Test
    fun `when search fails with parsing then the error message is shown and reported once`() = runTest {
        products.result = Outcome.Failure(AppError.Parsing)
        val viewModel = createViewModel(initialQuery = "590")

        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertEquals(AppEvent.ShowMessage(AppError.Parsing.toUiText(), MessageTone.Error), appEvents.last())
        assertEquals(AppError.Parsing, errorReporter.reports.single().error)
    }

    @Test
    fun `when a barcode is scanned while scanning is paused then it is ignored`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))

        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC), screenEvents)
    }

    @Test
    fun `when started with a torch then torch availability reaches the state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = true))

        assertEquals(preview(torch = TORCH_OFF), viewModel.state.value.camera)
    }

    @Test
    fun `when torch is clicked then it toggles and is saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = true))

        viewModel.onAction(ScanAction.OnTorchClicked)
        assertEquals(preview(torch = TORCH_ON, isTorchOn = true), viewModel.state.value.camera)
        assertEquals(true, handle.get<Boolean>(KEY_TORCH))

        viewModel.onAction(ScanAction.OnTorchClicked)
        assertEquals(preview(torch = TORCH_OFF), viewModel.state.value.camera)
        assertEquals(false, handle.get<Boolean>(KEY_TORCH))
    }

    @Test
    fun `when handle has torch and type sheet state then both are restored`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_TORCH to true, KEY_TYPE_SHEET_OPEN to true)))

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = true))

        assertEquals(
            preview(
                isPaused = true,
                frame = ScanFrameMode.Idle,
                torch = TORCH_ON,
                isTorchOn = true,
                isTypeSheetOpen = true,
            ),
            viewModel.state.value.camera,
        )
    }

    @Test
    fun `when type it is clicked and the sheet is dismissed then it opens and closes and is saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnTypeItClicked)
        assertEquals(
            preview(isPaused = true, frame = ScanFrameMode.Idle, isTypeSheetOpen = true),
            viewModel.state.value.camera,
        )
        assertEquals(true, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))

        viewModel.onAction(ScanAction.OnTypeSheetDismissed)
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertEquals(false, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))
    }

    @Test
    fun `when a typed lookup succeeds then the sheet closes and the found card shows`() = runTest {
        analyzeProduct.emit(ratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle, initialQuery = "590")
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnTypeItClicked)

        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertEquals(listOf<ScanEvent>(FOUND_HAPTIC), screenEvents)
        assertEquals(false, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))
        assertEquals(ratedCard, viewModel.state.value.found)
        assertEquals(preview(isPaused = true, frame = ScanFrameMode.Found), viewModel.state.value.camera)
    }

    @Test
    fun `when a typed lookup is not found then the sheet closes and the not found card shows`() = runTest {
        products.result = Outcome.Failure(AppError.NotFound)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle, initialQuery = "590")
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnTypeItClicked)

        viewModel.onAction(ScanAction.OnSearchSubmitted)
        advanceTimeBy(2_000)
        runCurrent()

        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEvents)
        assertEquals(notFoundCard("590"), viewModel.state.value.notFound)
        assertEquals(preview(isPaused = true, frame = ScanFrameMode.NotFound), viewModel.state.value.camera)
        assertEquals(false, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))
    }

    @Test
    fun `when a typed lookup fails with a network error then the sheet stays open and the message is shown`() =
        runTest {
            products.result = Outcome.Failure(AppError.Network)
            val handle = SavedStateHandle()
            val viewModel = createViewModel(handle, initialQuery = "590")
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
            viewModel.onAction(ScanAction.OnTypeItClicked)

            viewModel.onAction(ScanAction.OnSearchSubmitted)
            advanceTimeBy(2_000)
            runCurrent()

            assertEquals(AppEvent.ShowMessage(AppError.Network.toUiText(), MessageTone.Error), appEvents.last())
            assertNull(viewModel.state.value.notFound)
            assertEquals(
                preview(isPaused = true, frame = ScanFrameMode.Idle, isTypeSheetOpen = true),
                viewModel.state.value.camera,
            )
            assertEquals(true, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))
        }

    @Test
    fun `when a typed lookup fails with a timeout then the sheet stays open and the message is shown`() =
        runTest {
            products.result = Outcome.Failure(AppError.Timeout)
            val handle = SavedStateHandle()
            val viewModel = createViewModel(handle = handle, initialQuery = "590")
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
            viewModel.onAction(ScanAction.OnTypeItClicked)

            viewModel.onAction(ScanAction.OnSearchSubmitted)
            advanceTimeBy(2_000)
            runCurrent()

            assertEquals(AppEvent.ShowMessage(AppError.Timeout.toUiText(), MessageTone.Error), appEvents.last())
            assertNull(viewModel.state.value.notFound)
            assertEquals(
                preview(isPaused = true, frame = ScanFrameMode.Idle, isTypeSheetOpen = true),
                viewModel.state.value.camera,
            )
            assertEquals(true, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))
        }

    @Test
    fun `when a barcode is scanned while the type sheet is open then it is ignored`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnTypeItClicked)

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertTrue(products.refreshedBarcodes.isEmpty())
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when a scan succeeds then the card shows after analysis with progress until then and history is saved once`() =
        runTest {
            val handle = SavedStateHandle()
            val viewModel = createViewModel(handle)
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals("590", handle.get<String>(KEY_FOUND_BARCODE))
            assertFalse(viewModel.state.value.isLookUpEnabled)
            assertNull(viewModel.state.value.found)
            assertEquals(
                preview(isPaused = true, frame = ScanFrameMode.Searching, hint = SEARCHING_HINT),
                viewModel.state.value.camera,
            )
            assertTrue(history.saved.isEmpty())

            analyzeProduct.emit(ratedAnalysis)

            assertTrue(viewModel.state.value.isLookUpEnabled)
            assertEquals(ratedCard, viewModel.state.value.found)
            assertEquals(preview(isPaused = true, frame = ScanFrameMode.Found), viewModel.state.value.camera)
            assertEquals(listOf(ratedAnalysis.product), history.saved)

            analyzeProduct.emit(ratedAnalysis.copy(product = product.copy(name = "Nutella B-ready")))

            assertEquals(1, history.saved.size)
        }

    @Test
    fun `when details is clicked then product opens the card is cleared and scanning resumes on return`() = runTest {
        analyzeProduct.emit(ratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnDetailsClicked)

        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, FOUND_HAPTIC, NavigateToProduct("590")), screenEvents)
        assertNull(viewModel.state.value.found)
        assertNull(handle.get<String>(KEY_FOUND_BARCODE))
        assertEquals(preview(isPaused = true, frame = ScanFrameMode.Idle), viewModel.state.value.camera)

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(1, history.saved.size)
        assertEquals(listOf("590"), products.refreshedBarcodes)

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
    }

    @Test
    fun `when scan next is clicked then the card and the handle key are cleared and the preview resumes`() = runTest {
        analyzeProduct.emit(ratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnScanNextClicked)

        assertNull(viewModel.state.value.found)
        assertNull(handle.get<String>(KEY_FOUND_BARCODE))
        assertTrue(viewModel.state.value.isLookUpEnabled)
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)

        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))

        assertEquals(listOf("590", "123"), products.refreshedBarcodes)
    }

    @Test
    fun `when restored with a found barcode then the card shows from the cache without a lookup or history entry`() =
        runTest {
            analyzeProduct.emit(ratedAnalysis)
            val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_FOUND_BARCODE to "590")))

            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            assertEquals(ratedCard, viewModel.state.value.found)
            assertEquals(preview(isPaused = true, frame = ScanFrameMode.Found), viewModel.state.value.camera)
            assertTrue(products.refreshedBarcodes.isEmpty())
            assertTrue(history.saved.isEmpty())
        }

    @Test
    fun `when the card is shown then scans and submits are ignored`() = runTest {
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel(initialQuery = "590")
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnSearchSubmitted)

        viewModel.onAction(ScanAction.OnSearchSubmitted)
        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))

        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertEquals(ratedCard, viewModel.state.value.found)
        assertEquals(1, history.saved.size)
    }

    @Test
    fun `when the found analysis fails then the card clears the message shows it is reported once and scan resumes`() =
        runTest {
            analyzeProduct.failure = IllegalStateException("disk")
            val handle = SavedStateHandle()
            val viewModel = createViewModel(handle)
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertNull(viewModel.state.value.found)
            assertNull(handle.get<String>(KEY_FOUND_BARCODE))
            assertTrue(viewModel.state.value.isLookUpEnabled)
            assertEquals(
                listOf(AppEvent.HideKeyboard, AppEvent.ShowMessage(AppError.Storage.toUiText(), MessageTone.Error)),
                appEvents,
            )
            assertEquals(AppError.Storage, errorReporter.reports.single().error)
            assertTrue(history.saved.isEmpty())
            assertEquals(preview(isPaused = true, frame = ScanFrameMode.Idle), viewModel.state.value.camera)

            advanceTimeBy(2_000)
            runCurrent()
            assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        }

    @Test
    fun `when the history save fails then it is reported and the card stays`() = runTest {
        history.result = Outcome.Failure(AppError.Storage)
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(ratedCard, viewModel.state.value.found)
        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEvents)
        assertEquals("Saving history entry failed", errorReporter.reports.single().message)
    }

    @Test
    fun `when the analysis has insufficient data then the card shows the unknown rating with its title and detail`() =
        runTest {
            analyzeProduct.emit(ProductAnalysis.InsufficientData(product))
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(
                FoundCardState(
                    name = UiText.Raw("Nutella"),
                    icon = AppIcons.Bag,
                    brand = UiText.Raw("Ferrero"),
                    rating = Rating.Unknown,
                    title = UiText.Res(R.string.scan_verdict_insufficient_data),
                    detail = UiText.Res(R.string.scan_found_insufficient_detail),
                    showVerdictTip = false,
                ),
                viewModel.state.value.found,
            )
        }

    @Test
    fun `when created then three trip entries are requested once`() = runTest {
        createViewModel()

        assertEquals(listOf(3), history.requestedAmounts)
    }

    @Test
    fun `when trip entries are emitted then the preview shows the ones inside the window`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        history.entries.value = Outcome.Success(
            listOf(entry("1"), entry("2", updateTime = NOW - 2.hours), entry("3", updateTime = NOW - 4.hours)),
        )

        assertEquals(listOf("1", "2"), viewModel.tripBarcodes())
    }

    @Test
    fun `when the clock moves and the screen starts again then aged out entries are hidden`() = runTest {
        history.entries.value = Outcome.Success(listOf(entry("1"), entry("2", updateTime = NOW - 2.hours)))
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        assertEquals(listOf("1", "2"), viewModel.tripBarcodes())

        clock.now = NOW + 1.hours + 30.minutes
        assertEquals(listOf("1", "2"), viewModel.tripBarcodes())

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        assertEquals(listOf("1"), viewModel.tripBarcodes())
    }

    @Test
    fun `when new trip entries arrive then the time is refreshed`() = runTest {
        history.entries.value = Outcome.Success(listOf(entry("1")))
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        clock.now = NOW + 3.hours
        history.entries.value = Outcome.Success(listOf(entry("2", updateTime = NOW + 3.hours), entry("1")))

        assertEquals(listOf("2"), viewModel.tripBarcodes())
    }

    @Test
    fun `when a trip item is clicked then product opens without a lookup or history entry`() = runTest {
        history.entries.value = Outcome.Success(listOf(entry("123")))
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnTripItemClicked("123"))

        assertEquals(listOf<ScanEvent>(NavigateToProduct("123")), screenEvents)
        assertTrue(products.refreshedBarcodes.isEmpty())
        assertTrue(history.saved.isEmpty())
    }

    @Test
    fun `when a found card is pending or shown then the trip is hidden and returns with the scan after scan next`() =
        runTest {
            history.entries.value = Outcome.Success(listOf(entry("1")))
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
            assertTrue(viewModel.tripBarcodes().isEmpty())

            analyzeProduct.emit(ratedAnalysis)
            history.entries.value = Outcome.Success(listOf(entry("590"), entry("1")))
            assertEquals(ratedCard, viewModel.state.value.found)
            assertTrue(viewModel.tripBarcodes().isEmpty())

            viewModel.onAction(ScanAction.OnScanNextClicked)
            assertEquals(listOf("590", "1"), viewModel.tripBarcodes())
        }

    @Test
    fun `when loading trip entries fails then it is reported and retried after 5 seconds`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        history.entries.value = Outcome.Failure(AppError.Storage)

        assertEquals(AppError.Storage, errorReporter.reports.single().error)
        assertTrue(appEvents.isEmpty())
        assertTrue(viewModel.tripBarcodes().isEmpty())

        history.entries.value = Outcome.Success(listOf(entry("1")))
        advanceTimeBy(4_999)
        runCurrent()
        assertEquals(1, history.requestedAmounts.size)
        assertTrue(viewModel.tripBarcodes().isEmpty())

        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, history.requestedAmounts.size)
        assertEquals(listOf("1"), viewModel.tripBarcodes())
        assertEquals(1, errorReporter.reports.size)
    }

    @Test
    fun `when low carb preferences load then the trip description follows them`() = runTest {
        history.entries.value = Outcome.Success(listOf(entry("1")))
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        assertEquals(UiText.Res(R.string.scan_verdict_good_keto), tripItems(viewModel).single().description)

        preferences.emit(Outcome.Success(DietPreferences(40, DietMode.LOW_CARB)))

        assertEquals(UiText.Res(R.string.scan_verdict_good_low_carb), tripItems(viewModel).single().description)
    }

    @Test
    fun `when a camera scan is accepted then a detected haptic is sent once and the frame searches`() =
        runTest {
            val gate = products.gate()
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC), screenEvents)
            assertEquals(
                preview(isPaused = true, frame = ScanFrameMode.Searching, hint = SEARCHING_HINT),
                viewModel.state.value.camera,
            )
            gate.complete(Unit)
        }

    @Test
    fun `when a typed search runs then no detected haptic is sent`() = runTest {
        val gate = products.gate()
        val viewModel = createViewModel(initialQuery = "590")
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertTrue(screenEvents.isEmpty())
        assertEquals(
            preview(isPaused = true, frame = ScanFrameMode.Searching, hint = SEARCHING_HINT),
            viewModel.state.value.camera,
        )
        gate.complete(Unit)
    }

    @Test
    fun `when a scan is ignored while blocked then nothing is sent`() = runTest {
        val gate = products.gate()
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))

        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC), screenEvents)
        gate.complete(Unit)
    }

    @Test
    fun `when a scanned product is found then the found haptic is sent once after the analysis arrives`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC), screenEvents)

        analyzeProduct.emit(ratedAnalysis)
        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, FOUND_HAPTIC), screenEvents)
        assertEquals(preview(isPaused = true, frame = ScanFrameMode.Found), viewModel.state.value.camera)

        analyzeProduct.emit(ratedAnalysis.copy(product = product.copy(name = "Nutella B-ready")))
        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, FOUND_HAPTIC), screenEvents)
    }

    @Test
    fun `when a scanned product is not found then the not found haptic is sent and the frame shows not found`() =
        runTest {
            products.result = Outcome.Failure(AppError.NotFound)
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, NOT_FOUND_HAPTIC), screenEvents)
            assertEquals(preview(isPaused = true, frame = ScanFrameMode.NotFound), viewModel.state.value.camera)
        }

    @Test
    fun `when a scan fails with a network error then no verdict haptic is sent and the frame aims on resume`() =
        runTest {
            products.result = Outcome.Failure(AppError.Network)
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC), screenEvents)
            assertEquals(preview(isPaused = true, frame = ScanFrameMode.Idle), viewModel.state.value.camera)

            advanceTimeBy(2_000)
            runCurrent()

            assertEquals(preview(isPaused = false), viewModel.state.value.camera)
            assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC), screenEvents)
        }

    @Test
    fun `when a scan fails with a timeout then the message is shown and the same barcode can be scanned again`() =
        runTest {
            products.result = Outcome.Failure(AppError.Timeout)
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(AppEvent.ShowMessage(AppError.Timeout.toUiText(), MessageTone.Error), appEvents.last())

            advanceTimeBy(2_000)
            runCurrent()

            assertEquals(preview(isPaused = false), viewModel.state.value.camera)

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(listOf("590", "590"), products.refreshedBarcodes)
        }

    @Test
    fun `when restored with a found barcode then no haptic is sent`() = runTest {
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_FOUND_BARCODE to "590")))

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(ratedCard, viewModel.state.value.found)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when tips are pending then the first scan hint reaches the state after creation`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.FIRST_SCAN_HINT))
        val viewModel = createViewModel()

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(FIRST_HINT_KETO, hint(viewModel))
    }

    @Test
    fun `when loading tips fails then it is reported once and no tip shows`() = runTest {
        onboarding.pendingTipsResult = Outcome.Failure(AppError.Storage)
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(AIM_HINT, hint(viewModel))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals("Loading onboarding tips failed", errorReporter.reports.single().message)
        assertEquals(ratedCard, viewModel.state.value.found)
        assertTrue(onboarding.shownTips.isEmpty())
    }

    @Test
    fun `when the first lookup succeeds then the first scan hint is marked once and the hint asks to aim`() =
        runTest {
            onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.FIRST_SCAN_HINT))
            analyzeProduct.emit(ratedAnalysis)
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
            viewModel.onAction(ScanAction.OnScanNextClicked)

            assertEquals(listOf(OnboardingTip.FIRST_SCAN_HINT), onboarding.shownTips)
            assertEquals(AIM_HINT, hint(viewModel))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(listOf(OnboardingTip.FIRST_SCAN_HINT), onboarding.shownTips)
        }

    @Test
    fun `when a lookup is not found or fails then the first scan hint stays pending`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.FIRST_SCAN_HINT))
        products.result = Outcome.Failure(AppError.NotFound)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        viewModel.onAction(ScanAction.OnScanNextClicked)
        products.result = Outcome.Failure(AppError.Network)
        viewModel.onAction(ScanAction.OnBarcodeScanned("591"))
        advanceTimeBy(2_000)
        runCurrent()

        assertTrue(onboarding.shownTips.isEmpty())
        assertEquals(FIRST_HINT_KETO, hint(viewModel))
    }

    @Test
    fun `when got it is clicked and the card closes then the verdict tip is marked only once`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(ratedCard.copy(showVerdictTip = true), viewModel.state.value.found)

        viewModel.onAction(ScanAction.OnVerdictTipDismissed)
        viewModel.onAction(ScanAction.OnVerdictTipDismissed)
        viewModel.onAction(ScanAction.OnScanNextClicked)

        assertEquals(listOf(OnboardingTip.VERDICT_TIP), onboarding.shownTips)
        assertNull(viewModel.state.value.found)
    }

    @Test
    fun `when got it is clicked then the found card stays with the tip hidden`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnVerdictTipDismissed)

        assertEquals(ratedCard, viewModel.state.value.found)
    }

    @Test
    fun `when scan next is clicked with the verdict tip visible then the tip is marked`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnScanNextClicked)
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(listOf(OnboardingTip.VERDICT_TIP), onboarding.shownTips)
        assertEquals(ratedCard, viewModel.state.value.found)
    }

    @Test
    fun `when details is clicked with the verdict tip visible then the tip is marked`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnDetailsClicked)

        assertEquals(listOf(OnboardingTip.VERDICT_TIP), onboarding.shownTips)
    }

    @Test
    fun `when scan next is clicked on an insufficient data card then the verdict tip stays pending`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
        analyzeProduct.emit(ProductAnalysis.InsufficientData(product))
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertFalse(assertIs<FoundCardState>(viewModel.state.value.found).showVerdictTip)

        viewModel.onAction(ScanAction.OnScanNextClicked)
        analyzeProduct.emit(ratedAnalysis)
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertTrue(onboarding.shownTips.isEmpty())
        assertEquals(ratedCard.copy(showVerdictTip = true), viewModel.state.value.found)
    }

    @Test
    fun `when saving a shown tip fails then it is only reported and the tip stays hidden`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
        onboarding.markResult = Outcome.Failure(AppError.Storage)
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnVerdictTipDismissed)

        assertEquals("Saving shown tip failed", errorReporter.reports.single().message)
        assertEquals(ratedCard, viewModel.state.value.found)
        assertTrue(appEvents.none { it is AppEvent.ShowMessage })
    }

    @Test
    fun `when no tips are pending then no tip is ever marked`() = runTest {
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        viewModel.onAction(ScanAction.OnVerdictTipDismissed)
        viewModel.onAction(ScanAction.OnScanNextClicked)
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        viewModel.onAction(ScanAction.OnDetailsClicked)

        assertTrue(onboarding.shownTips.isEmpty())
    }

    @Test
    fun `when a product is scanned without a diet then its page opens once without a card and it is saved once`() =
        runTest {
            preferences.emit(Outcome.Success(DietPreferences.NoDiet))
            onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.FIRST_SCAN_HINT))
            val handle = SavedStateHandle()
            val viewModel = createViewModel(handle)
            val states = collectStates(viewModel)
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(searchingPreview, viewModel.state.value.camera)
            val statesBefore = states.size - 1

            analyzeProduct.emit(unratedAnalysis)

            assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, FOUND_HAPTIC, NavigateToProduct("590")), screenEvents)
            assertEquals(listOf(product), history.saved)
            assertEquals(listOf(OnboardingTip.FIRST_SCAN_HINT), onboarding.shownTips)
            assertNull(handle.get<String>(KEY_FOUND_BARCODE))
            assertEquals(true, handle.get<Boolean>(KEY_RESUMING))
            assertTrue(viewModel.state.value.isLookUpEnabled)
            assertEquals(waitingPreview, viewModel.state.value.camera)
            assertTrue(states.all { it.found == null })
            assertEquals(
                listOf(ScanFrameMode.Searching, ScanFrameMode.Waiting),
                frames(states.drop(statesBefore)).distinct(),
            )

            analyzeProduct.emit(ratedAnalysis)

            assertEquals(3, screenEvents.size)
            assertEquals(1, history.saved.size)
            assertTrue(states.all { it.found == null })
        }

    @Test
    fun `when a product is typed without a diet then the sheet closes and its page opens once`() = runTest {
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        analyzeProduct.emit(unratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        val states = collectStates(viewModel)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnTypeItClicked)
        viewModel.onAction(ScanAction.OnQueryChanged("590"))

        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertEquals(listOf<ScanEvent>(FOUND_HAPTIC, NavigateToProduct("590")), screenEvents)
        assertEquals(listOf(product), history.saved)
        assertNull(handle.get<String>(KEY_FOUND_BARCODE))
        assertEquals(false, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))
        assertEquals(waitingPreview, viewModel.state.value.camera)
        assertTrue(states.all { it.found == null })
    }

    @Test
    fun `when a product with missing data is found without a diet then its page opens once`() = runTest {
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        analyzeProduct.emit(ProductAnalysis.InsufficientData(product = product, hasDiet = false))
        val viewModel = createViewModel()
        val states = collectStates(viewModel)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, FOUND_HAPTIC, NavigateToProduct("590")), screenEvents)
        assertEquals(listOf(product), history.saved)
        assertEquals(waitingPreview, viewModel.state.value.camera)
        assertTrue(states.all { it.found == null })
    }

    @Test
    fun `when the history save fails without a diet then it is reported and the page still opens`() = runTest {
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        history.result = Outcome.Failure(AppError.Storage)
        analyzeProduct.emit(unratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals("Saving history entry failed", errorReporter.reports.single().message)
        assertEquals(NavigateToProduct("590"), screenEvents.last())
        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEvents)
    }

    @Test
    fun `when back from a page that opened directly then scanning starts again after 2 seconds`() = runTest {
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        analyzeProduct.emit(unratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        openDirectly(viewModel)
        viewModel.onAction(ScanAction.OnStop)
        advanceTimeBy(10_000)
        runCurrent()

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(waitingPreview, viewModel.state.value.camera)
        advanceTimeBy(1_999)
        runCurrent()
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        assertEquals(waitingPreview, viewModel.state.value.camera)
        assertEquals(listOf("590"), products.refreshedBarcodes)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertEquals(false, handle.get<Boolean>(KEY_RESUMING))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        assertEquals(listOf("590", "590"), products.refreshedBarcodes)
        assertEquals(2, history.saved.size)
    }

    @Test
    fun `when Scan is left within the 2 seconds then they start again on the next return`() = runTest {
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        analyzeProduct.emit(unratedAnalysis)
        val viewModel = createViewModel()
        openDirectly(viewModel)
        viewModel.onAction(ScanAction.OnStop)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        advanceTimeBy(1_500)
        runCurrent()
        viewModel.onAction(ScanAction.OnStop)

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        advanceTimeBy(1_999)
        runCurrent()
        assertEquals(waitingPreview, viewModel.state.value.camera)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
    }

    @Test
    fun `when the page did not open then scanning starts again after 2 seconds`() = runTest {
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        analyzeProduct.emit(unratedAnalysis)
        val viewModel = createViewModel()
        openDirectly(viewModel)

        advanceTimeBy(2_000)
        runCurrent()

        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
    }

    @Test
    fun `when a product is typed within the 2 seconds then it gets its own full wait`() = runTest {
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        analyzeProduct.emit(unratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        openDirectly(viewModel)
        viewModel.onAction(ScanAction.OnStop)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        advanceTimeBy(1_000)
        runCurrent()
        val gate = products.gate()
        viewModel.onAction(ScanAction.OnTypeItClicked)
        viewModel.onAction(ScanAction.OnQueryChanged("590"))

        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertEquals(false, handle.get<Boolean>(KEY_RESUMING))
        advanceTimeBy(500)
        runCurrent()
        gate.complete(Unit)
        runCurrent()
        assertEquals(2, screenEvents.count { it == NavigateToProduct("590") })
        assertEquals(waitingPreview, viewModel.state.value.camera)
        advanceTimeBy(1_999)
        runCurrent()
        assertEquals(waitingPreview, viewModel.state.value.camera)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
    }

    @Test
    fun `when the search ends while Scan is stopped then the 2 seconds start after the return`() = runTest {
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        analyzeProduct.emit(unratedAnalysis)
        val gate = products.gate()
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        viewModel.onAction(ScanAction.OnStop)

        gate.complete(Unit)
        advanceTimeBy(10_000)
        runCurrent()

        assertEquals(NavigateToProduct("590"), screenEvents.last())
        assertEquals(waitingPreview, viewModel.state.value.camera)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnStop)
        advanceTimeBy(10_000)
        runCurrent()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        advanceTimeBy(1_999)
        runCurrent()
        assertEquals(waitingPreview, viewModel.state.value.camera)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
    }

    @Test
    fun `when restored while waiting then scanning starts again 2 seconds after the start`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_RESUMING to true)))
        advanceTimeBy(10_000)
        runCurrent()

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(waitingPreview, viewModel.state.value.camera)
        advanceTimeBy(2_000)
        runCurrent()
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
    }

    @Test
    fun `when restored with a found barcode without a diet then nothing opens and scanning is free`() = runTest {
        analyzeProduct.emit(unratedAnalysis)
        val handle = SavedStateHandle(mapOf(KEY_FOUND_BARCODE to "590"))
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        assertNull(viewModel.state.value.found)

        preferences.emit(Outcome.Success(DietPreferences.NoDiet))

        assertNull(viewModel.state.value.found)
        assertNull(handle.get<String>(KEY_FOUND_BARCODE))
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertTrue(screenEvents.isEmpty())
        assertTrue(history.saved.isEmpty())
    }

    @Test
    fun `when the diet is switched off while a card waits then it closes at once before the analysis follows`() =
        runTest {
            onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
            analyzeProduct.emit(ratedAnalysis)
            val handle = SavedStateHandle()
            val viewModel = createViewModel(handle)
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
            val sentBefore = screenEvents.toList()
            val states = collectStates(viewModel)

            preferences.emit(Outcome.Success(DietPreferences.NoDiet))

            assertEquals(
                listOf(ratedCard.copy(showVerdictTip = true), null),
                states.map { it.found },
            )
            assertEquals(preview(isPaused = false), viewModel.state.value.camera)
            assertNull(handle.get<String>(KEY_FOUND_BARCODE))
            assertEquals(sentBefore, screenEvents)
            assertTrue(onboarding.shownTips.isEmpty())

            analyzeProduct.emit(unratedAnalysis)

            assertEquals(2, states.size)
            assertEquals(1, history.saved.size)
            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
            assertEquals(NavigateToProduct("590"), screenEvents.last())
        }

    @Test
    fun `when preferences with a diet load while a card waits then the card stays`() = runTest {
        analyzeProduct.emit(ratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        preferences.emit(Outcome.Success(DietPreferences(maxDailyCarbsAmount = 100, dietMode = DietMode.LOW_CARB)))

        assertEquals(ratedCard, viewModel.state.value.found)
        assertEquals("590", handle.get<String>(KEY_FOUND_BARCODE))
    }

    @Test
    fun `when a product is not found without a diet then its card stays when No diet loads`() = runTest {
        products.result = Outcome.Failure(AppError.NotFound)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        preferences.emit(Outcome.Success(DietPreferences.NoDiet))

        assertEquals(notFoundCard("590"), viewModel.state.value.notFound)
        assertEquals(preview(isPaused = true, frame = ScanFrameMode.NotFound), viewModel.state.value.camera)
        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, NOT_FOUND_HAPTIC), screenEvents)
        assertTrue(history.saved.isEmpty())
    }

    @Test
    fun `when a search fails without a diet then the message shows and scanning resumes as before`() = runTest {
        products.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(AppEvent.ShowMessage(AppError.Network.toUiText(), MessageTone.Error), appEvents.last())
        assertEquals(preview(isPaused = true, frame = ScanFrameMode.Idle), viewModel.state.value.camera)
        advanceTimeBy(2_000)
        runCurrent()
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC), screenEvents)
    }

    @Test
    fun `when the found analysis fails without a diet then nothing opens and scanning resumes`() = runTest {
        analyzeProduct.failure = IllegalStateException("disk")
        val viewModel = createViewModel()
        preferences.emit(Outcome.Success(DietPreferences.NoDiet))
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC), screenEvents)
        assertTrue(history.saved.isEmpty())
        assertEquals(AppError.Storage, errorReporter.reports.single().error)
        advanceTimeBy(2_000)
        runCurrent()
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
    }

    @Test
    fun `when pages opened directly then the verdict tip is pending and shows on the first card with a diet`() =
        runTest {
            preferences.emit(Outcome.Success(DietPreferences.NoDiet))
            onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
            analyzeProduct.emit(unratedAnalysis)
            val viewModel = createViewModel()
            openDirectly(viewModel)
            viewModel.onAction(ScanAction.OnStop)
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
            advanceTimeBy(2_000)
            runCurrent()
            assertTrue(onboarding.shownTips.isEmpty())

            analyzeProduct.emit(ratedAnalysis)
            preferences.emit(Outcome.Success(DietPreferences.Default))
            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(ratedCard.copy(showVerdictTip = true), viewModel.state.value.found)
        }

    @Test
    fun `when a product is scanned with details right away then its page opens once without a card`() = runTest {
        preferences.emit(detailsRightAway)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        val states = collectStates(viewModel)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(searchingPreview, viewModel.state.value.camera)
        val statesBefore = states.size - 1

        analyzeProduct.emit(ratedAnalysis)

        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, FOUND_HAPTIC, NavigateToProduct("590")), screenEvents)
        assertEquals(listOf(product), history.saved)
        assertNull(handle.get<String>(KEY_FOUND_BARCODE))
        assertEquals(true, handle.get<Boolean>(KEY_RESUMING))
        assertTrue(viewModel.state.value.isLookUpEnabled)
        assertEquals(waitingPreview, viewModel.state.value.camera)
        assertEquals(
            listOf(ScanFrameMode.Searching, ScanFrameMode.Waiting),
            frames(states.drop(statesBefore)).distinct(),
        )

        analyzeProduct.emit(ratedAnalysis.copy(product = product.copy(name = "Nutella B-ready")))

        assertEquals(3, screenEvents.size)
        assertEquals(1, history.saved.size)
        assertTrue(states.all { it.found == null })
        assertFalse(ScanFrameMode.Found in frames(states))
    }

    @Test
    fun `when a product is typed with details right away then the sheet closes and its page opens once`() = runTest {
        preferences.emit(detailsRightAway)
        analyzeProduct.emit(ratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        val states = collectStates(viewModel)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnTypeItClicked)
        viewModel.onAction(ScanAction.OnQueryChanged("590"))

        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertEquals(listOf<ScanEvent>(FOUND_HAPTIC, NavigateToProduct("590")), screenEvents)
        assertEquals(listOf(product), history.saved)
        assertNull(handle.get<String>(KEY_FOUND_BARCODE))
        assertEquals(false, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))
        assertEquals(waitingPreview, viewModel.state.value.camera)
        assertTrue(states.all { it.found == null })
        assertFalse(ScanFrameMode.Found in frames(states))
    }

    @Test
    fun `when a product with missing data is found with details right away then its page opens once`() = runTest {
        preferences.emit(detailsRightAway)
        analyzeProduct.emit(ProductAnalysis.InsufficientData(product))
        val viewModel = createViewModel()
        val states = collectStates(viewModel)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, FOUND_HAPTIC, NavigateToProduct("590")), screenEvents)
        assertEquals(listOf(product), history.saved)
        assertTrue(states.all { it.found == null })
    }

    @Test
    fun `when back from a page that opened right away then scanning starts again after 2 seconds`() = runTest {
        preferences.emit(detailsRightAway)
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        openDirectly(viewModel)
        viewModel.onAction(ScanAction.OnStop)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        advanceTimeBy(1_999)
        runCurrent()
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        assertEquals(waitingPreview, viewModel.state.value.camera)
        assertEquals(listOf("590"), products.refreshedBarcodes)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
    }

    @Test
    fun `when a product is not found with details right away then its card shows and nothing opens`() = runTest {
        preferences.emit(detailsRightAway)
        products.result = Outcome.Failure(AppError.NotFound)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(notFoundCard("590"), viewModel.state.value.notFound)
        assertEquals(preview(isPaused = true, frame = ScanFrameMode.NotFound), viewModel.state.value.camera)
        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, NOT_FOUND_HAPTIC), screenEvents)
        assertTrue(history.saved.isEmpty())
    }

    @Test
    fun `when a search fails with details right away then the message shows and scanning resumes`() = runTest {
        preferences.emit(detailsRightAway)
        products.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(AppEvent.ShowMessage(AppError.Network.toUiText(), MessageTone.Error), appEvents.last())
        assertEquals(preview(isPaused = true, frame = ScanFrameMode.Idle), viewModel.state.value.camera)
        advanceTimeBy(2_000)
        runCurrent()
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC), screenEvents)
    }

    @Test
    fun `when marking the first scan hint brings the same settings again then the page still opens`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.FIRST_SCAN_HINT))
        onboarding.onTipShown = { preferences.emit(detailsRightAway) }
        preferences.emit(detailsRightAway)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        assertEquals(listOf(OnboardingTip.FIRST_SCAN_HINT), onboarding.shownTips)
        assertEquals(searchingPreview, viewModel.state.value.camera)

        analyzeProduct.emit(ratedAnalysis)

        assertEquals(NavigateToProduct("590"), screenEvents.last())
        assertEquals(listOf(product), history.saved)
    }

    @Test
    fun `when details right away is switched on while a card waits then it closes and scanning is free`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
        analyzeProduct.emit(ratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        val sentBefore = screenEvents.toList()
        val states = collectStates(viewModel)

        preferences.emit(detailsRightAway)

        assertEquals(listOf(ratedCard.copy(showVerdictTip = true), null), states.map { it.found })
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        assertNull(handle.get<String>(KEY_FOUND_BARCODE))
        assertEquals(sentBefore, screenEvents)
        assertTrue(onboarding.shownTips.isEmpty())
        assertEquals(1, history.saved.size)

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(NavigateToProduct("590"), screenEvents.last())
        assertEquals(2, history.saved.size)
    }

    @Test
    fun `when the same settings with cards are emitted again while a card waits then the card stays`() = runTest {
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        preferences.emit(ProfileSettings.Default)

        assertEquals(ratedCard, viewModel.state.value.found)
    }

    @Test
    fun `when details right away is switched off again then the next find shows the card with the tip`() = runTest {
        onboarding.pendingTipsResult = Outcome.Success(setOf(OnboardingTip.VERDICT_TIP))
        preferences.emit(detailsRightAway)
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        openDirectly(viewModel)
        resumeScanning(viewModel)
        assertTrue(onboarding.shownTips.isEmpty())

        preferences.emit(ProfileSettings.Default)
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(ratedCard.copy(showVerdictTip = true), viewModel.state.value.found)
    }

    @Test
    fun `when no diet is left then the stored choice decides whether the card shows`() = runTest {
        preferences.emit(ProfileSettings(dietPreferences = DietPreferences.NoDiet, opensDetailsRightAway = false))
        analyzeProduct.emit(unratedAnalysis)
        val viewModel = createViewModel()
        openDirectly(viewModel)
        resumeScanning(viewModel)

        analyzeProduct.emit(ratedAnalysis)
        preferences.emit(ProfileSettings.Default)
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        assertEquals(ratedCard, viewModel.state.value.found)
        viewModel.onAction(ScanAction.OnScanNextClicked)

        analyzeProduct.emit(unratedAnalysis)
        preferences.emit(ProfileSettings(dietPreferences = DietPreferences.NoDiet, opensDetailsRightAway = true))
        openDirectly(viewModel)
        resumeScanning(viewModel)

        analyzeProduct.emit(ratedAnalysis)
        preferences.emit(detailsRightAway)
        screenEvents.clear()
        openDirectly(viewModel)
        assertNull(viewModel.state.value.found)
    }

    @Test
    fun `when the settings load late then the lookup waits for them and the page opens without a card`() = runTest {
        preferences.forget()
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        val states = collectStates(viewModel)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))

        assertEquals(searchingPreview, viewModel.state.value.camera)
        assertFalse(viewModel.state.value.isLookUpEnabled)
        assertTrue(products.refreshedBarcodes.isEmpty())

        preferences.emit(detailsRightAway)

        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertEquals(listOf<ScanEvent>(DETECTED_HAPTIC, FOUND_HAPTIC, NavigateToProduct("590")), screenEvents)
        assertEquals(listOf(product), history.saved)
        assertTrue(states.all { it.found == null })
    }

    @Test
    fun `when the first read fails then the default shows the card and No diet after the retry closes it`() = runTest {
        preferences.fail(AppError.Storage)
        analyzeProduct.emit(ratedAnalysis)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        assertEquals("Loading diet preferences failed", errorReporter.reports.single().message)
        assertEquals(AppError.Storage, errorReporter.reports.single().error)

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        assertEquals(ratedCard, viewModel.state.value.found)

        preferences.emit(ProfileSettings.Default)
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(1, errorReporter.reports.size)
        assertEquals(ratedCard, viewModel.state.value.found)

        preferences.emit(Outcome.Success(DietPreferences.NoDiet))

        assertNull(viewModel.state.value.found)
        assertNull(handle.get<String>(KEY_FOUND_BARCODE))
        assertEquals(preview(isPaused = false), viewModel.state.value.camera)
    }

    @Test
    fun `when the first read fails then the setting counts after 5 seconds`() = runTest {
        preferences.fail(AppError.Storage)
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        preferences.emit(detailsRightAway)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        advanceTimeBy(4_999)
        runCurrent()
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        assertEquals(ratedCard, viewModel.state.value.found)

        advanceTimeBy(1)
        runCurrent()
        assertNull(viewModel.state.value.found)
        assertFalse(NavigateToProduct("590") in screenEvents)
        openDirectly(viewModel)
    }

    @Test
    fun `when a later read fails and brings the same settings then the setting stays and nothing closes`() = runTest {
        preferences.emit(detailsRightAway)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        preferences.fail(AppError.Storage)
        preferences.emit(detailsRightAway)
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(searchingPreview, viewModel.state.value.camera)

        analyzeProduct.emit(ratedAnalysis)

        assertEquals(1, errorReporter.reports.size)
        assertEquals(NavigateToProduct("590"), screenEvents.last())
    }

    private fun TestScope.resumeScanning(viewModel: ScanViewModel) {
        viewModel.onAction(ScanAction.OnStop)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        advanceTimeBy(2_000)
        runCurrent()
    }

    private fun openDirectly(viewModel: ScanViewModel) {
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))
        assertEquals(NavigateToProduct("590"), screenEvents.last())
    }

    private fun TestScope.collectStates(viewModel: ScanViewModel): List<ScanState> {
        val states = mutableListOf<ScanState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.toList(states) }
        return states
    }

    private fun frames(states: List<ScanState>): List<ScanFrameMode> =
        states.mapNotNull { (it.camera as? CameraState.Preview)?.frame }

    private fun hint(viewModel: ScanViewModel) = assertIs<CameraState.Preview>(viewModel.state.value.camera).hint

    private fun tripItems(viewModel: ScanViewModel) = assertIs<CameraState.Preview>(viewModel.state.value.camera).trip

    private fun notFoundCard(barcode: String) = NotFoundCardState(
        barcode = UiText.Res(R.string.scan_not_found_barcode, UiArg.Text(barcode)),
        message = UiText.Res(R.string.scan_not_found_message),
        showTypeIt = true,
        dismissLabel = UiText.Res(R.string.scan_next),
    )

    private fun preview(
        isPaused: Boolean = false,
        frame: ScanFrameMode = ScanFrameMode.Aiming,
        torch: TorchButtonState? = null,
        isTorchOn: Boolean = false,
        isTypeSheetOpen: Boolean = false,
        hint: UiText = UiText.Res(R.string.scan_aim_hint),
    ) = CameraState.Preview(
        isPaused = isPaused,
        isTorchOn = isTorchOn,
        torchButton = torch,
        isTypeSheetOpen = isTypeSheetOpen,
        trip = persistentListOf(),
        frame = frame,
        hint = CameraHintState(text = hint, supportingText = null),
    )

    private fun ScanViewModel.tripBarcodes(): List<String> = tripItems(this).map { it.barcode }

    private fun entry(barcode: String, updateTime: Instant = NOW - 10.minutes) = ProductAnalysisHistoryEntry(
        id = barcode.hashCode().toLong(),
        productName = "Name $barcode",
        productBarcode = barcode,
        productBrand = "Brand",
        updateTime = updateTime,
        dietRate = DietRate.GOOD,
        carbsPer100g = null,
        category = ProductCategory.OTHER,
    )

    private fun TestScope.createViewModel(
        handle: SavedStateHandle = SavedStateHandle(),
        initialQuery: String = "",
    ): ScanViewModel {
        val events = AppEvents(backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { events.events.toList(appEvents) }
        val resources = ScanResources()
        val viewModel = ScanViewModel(
            savedStateHandle = handle,
            products = products,
            analyzeProduct = analyzeProduct,
            history = history,
            appEvents = events,
            stateFactory = ScanStateFactory(resources),
            resources = resources,
            errorReporter = errorReporter,
            profileSettings = preferences,
            onboarding = onboarding,
            clock = clock,
            applicationScope = CoroutineScope(
                backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler),
            ),
            initialQuery = initialQuery,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { event -> screenEvents += event }
        }
        return viewModel
    }

    private class FakeProductRepository : ProductRepository {

        var result: Outcome<Unit> = Outcome.Success(Unit)
        val refreshedBarcodes = mutableListOf<String>()
        private var gate: CompletableDeferred<Unit>? = null

        fun gate(): CompletableDeferred<Unit> = CompletableDeferred<Unit>().also { gate = it }

        override fun observeProduct(barcode: String): Flow<Product> = throw NotImplementedError()

        override fun observeProducts(barcodes: Set<Barcode>): Flow<List<Product>> =
            throw NotImplementedError()

        override suspend fun refreshProduct(barcode: String): Outcome<Unit> {
            refreshedBarcodes += barcode
            gate?.await()
            return result
        }
    }

    private class FakeAnalyzeProduct : AnalyzeProduct {

        var failure: Throwable? = null
        private val updates = mutableMapOf<String, MutableSharedFlow<ProductAnalysis>>()

        fun emit(analysis: ProductAnalysis) {
            updatesFor(analysis.product.barcode).tryEmit(analysis)
        }

        private fun updatesFor(barcode: String) = updates.getOrPut(barcode) { MutableSharedFlow(replay = 1) }

        override fun invoke(barcode: String): Flow<ProductAnalysis> = flow {
            failure?.let { throw it }
            emitAll(updatesFor(barcode))
        }
    }

    private class FakeHistory : AnalysisHistoryRepository {

        var result: Outcome<Unit> = Outcome.Success(Unit)
        val saved = mutableListOf<Product>()
        val entries = MutableStateFlow<Outcome<List<ProductAnalysisHistoryEntry>>>(Outcome.Success(emptyList()))
        val requestedAmounts = mutableListOf<Int>()

        override suspend fun save(product: Product): Outcome<Unit> {
            saved += product
            return result
        }

        override fun observeLastUniqueEntries(amount: Int): Flow<List<ProductAnalysisHistoryEntry>> = flow {
            requestedAmounts += amount
            emitAll(entries.map { it.getOrThrow() })
        }
    }

    private class FakePreferences : ProfileSettingsRepository {

        private val flow = MutableSharedFlow<Outcome<ProfileSettings>>(replay = 1)

        suspend fun emit(outcome: Outcome<DietPreferences>) =
            flow.emit(outcome.map { ProfileSettings(dietPreferences = it) })

        suspend fun emit(settings: ProfileSettings) = flow.emit(Outcome.Success(settings))

        suspend fun fail(error: AppError) = flow.emit(Outcome.Failure(error))

        @OptIn(ExperimentalCoroutinesApi::class)
        fun forget() = flow.resetReplayCache()

        override fun observeSettings(): Flow<ProfileSettings> = flow.map { it.getOrThrow() }

        override suspend fun saveSettings(settings: ProfileSettings): Outcome<Unit> = throw NotImplementedError()

        init {
            flow.tryEmit(Outcome.Success(ProfileSettings.Default))
        }
    }

    private class FakeOnboarding : OnboardingRepository {

        var pendingTipsResult: Outcome<Set<OnboardingTip>> = Outcome.Success(emptySet())
        var markResult: Outcome<Unit> = Outcome.Success(Unit)
        val shownTips = mutableListOf<OnboardingTip>()
        var onTipShown: suspend () -> Unit = {}

        override suspend fun isSetupDone(): Outcome<Boolean> = throw NotImplementedError()

        override suspend fun completeSetup(preset: DietPreset): Outcome<Unit> = throw NotImplementedError()

        override suspend fun pendingTips(): Outcome<Set<OnboardingTip>> = pendingTipsResult

        override suspend fun markTipShown(tip: OnboardingTip): Outcome<Unit> {
            shownTips += tip
            onTipShown()
            return markResult
        }
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_CAMERA_PERMISSION = "cameraPermission"
        const val KEY_TORCH = "torch"
        const val KEY_TYPE_SHEET_OPEN = "typeSheetOpen"
        const val KEY_RESUMING = "resuming"
        const val KEY_FOUND_BARCODE = "foundBarcode"
        const val KEY_NOT_FOUND_BARCODE = "notFoundBarcode"
        val NOW = Instant.parse("2026-10-30T12:00:00Z")
        val SEARCHING_HINT = UiText.Res(R.string.scan_searching_hint)
        val AIM_HINT = CameraHintState(text = UiText.Res(R.string.scan_aim_hint), supportingText = null)
        val FIRST_HINT_KETO = CameraHintState(
            text = UiText.Res(R.string.scan_first_hint_title),
            supportingText = UiText.Res(R.string.scan_first_hint_body_keto),
        )
        val TORCH_OFF = TorchButtonState(isActive = false, contentDescription = UiText.Res(R.string.scan_torch_on))
        val TORCH_ON = TorchButtonState(isActive = true, contentDescription = UiText.Res(R.string.scan_torch_off))
        val DETECTED_HAPTIC = PerformHapticFeedback(ScanFeedback.Detected)
        val FOUND_HAPTIC = PerformHapticFeedback(ScanFeedback.Found)
        val NOT_FOUND_HAPTIC = PerformHapticFeedback(ScanFeedback.NotFound)

        val product = Product(
            name = "Nutella",
            barcode = "590",
            brand = "Ferrero",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, 57.5, null, null),
            imageUrl = null,
            servingAmount = null,
            category = ProductCategory.OTHER,
        )
        val ratedAnalysis = ProductAnalysis.Rated(
            product = product,
            macronutrients = MacronutrientAnalysisResult(
                dietMode = DietMode.KETO,
                rate = DietRate.NOT_ADVISED,
                reasons = emptyList(),
                carbsPer100g = 57.5,
                dailyCarbConsumption = 40,
            ),
        )
        val detailsRightAway =
            ProfileSettings(dietPreferences = DietPreferences.Default, opensDetailsRightAway = true)
        val unratedAnalysis = ProductAnalysis.Unrated(product)
        val ratedCard = FoundCardState(
            name = UiText.Raw("Nutella"),
            icon = AppIcons.Bag,
            brand = UiText.Raw("Ferrero"),
            rating = Rating.Negative,
            title = UiText.Res(R.string.scan_verdict_not_advised_keto),
            detail = UiText.Res(R.string.scan_found_carbs_per_100g, UiArg.Numeric.Grams(57.5)),
            showVerdictTip = false,
        )

        val GRANTED = CameraPermissionCheck(isGranted = true, shouldShowRationale = false)
        val RATIONALE = CameraPermissionCheck(isGranted = false, shouldShowRationale = true)
        val NOT_GRANTED = CameraPermissionCheck(isGranted = false, shouldShowRationale = false)

        val rationalePanel = CameraState.PermissionPanel(
            title = UiText.Res(R.string.scan_camera_permission_rationale_title),
            message = UiText.Res(R.string.scan_camera_permission_rationale),
            buttonLabel = UiText.Res(R.string.scan_camera_permission_allow),
        )
        val deniedPanel = CameraState.PermissionPanel(
            title = UiText.Res(R.string.scan_camera_permission_denied_title),
            message = UiText.Res(R.string.scan_camera_permission_denied),
            buttonLabel = UiText.Res(R.string.scan_camera_permission_open_settings),
        )
    }
}
