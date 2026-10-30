package pl.handsome.club.feature.scanner.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.testing.FakeAppLogger
import pl.handsome.club.core.testing.FakeErrorReporter
import pl.handsome.club.feature.analysis.api.AnalyzeProduct
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.feature.scanner.ui.event.OpenAppSettings
import pl.handsome.club.feature.scanner.ui.event.RequestCameraPermission
import pl.handsome.club.feature.scanner.ui.event.ScanAction
import pl.handsome.club.feature.scanner.ui.event.ScanEvent
import pl.handsome.club.feature.scanner.ui.event.ScanEvent.ScanNavigation.NavigateToProduct
import pl.handsome.club.feature.scanner.ui.state.CameraPermissionCheck
import pl.handsome.club.feature.scanner.ui.state.CameraState
import pl.handsome.club.feature.scanner.ui.state.FoundCardState
import pl.handsome.club.feature.scanner.ui.state.ScanStateFactory
import pl.handsome.club.feature.scanner.ui.state.Torch
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScanViewModelTest {

    private val products = FakeProductRepository()
    private val analyzeProduct = FakeAnalyzeProduct()
    private val history = FakeHistory()
    private val logger = FakeAppLogger()
    private val errorReporter = FakeErrorReporter()
    private val appEvents = mutableListOf<AppEvent>()
    private val screenEvents = mutableListOf<ScanEvent>()

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
            assertTrue(screenEvents.isEmpty())
            assertEquals(ratedCard, viewModel.state.value.found)
            assertEquals(preview(isPaused = true), viewModel.state.value.camera)

            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            assertEquals(preview(isPaused = true), viewModel.state.value.camera)
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
        assertTrue(viewModel.state.value.showProgress)
        viewModel.onAction(ScanAction.OnSearchSubmitted)
        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))
        gate.complete(Unit)
        analyzeProduct.emit(ratedAnalysis)

        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertFalse(viewModel.state.value.showProgress)
        assertEquals(ratedCard, viewModel.state.value.found)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when a typed search is pending and the screen starts again then camera stays paused`() = runTest {
        val gate = products.gate()
        val viewModel = createViewModel(initialQuery = "590")
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnSearchSubmitted)

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        assertEquals(preview(isPaused = true), viewModel.state.value.camera)
        gate.complete(Unit)
    }

    @Test
    fun `when a scanned product is not found then message is shown warning logged and scan resumes after 2 seconds`() =
        runTest {
            products.result = Outcome.Failure(AppError.NotFound)
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(
                listOf(AppEvent.HideKeyboard, AppEvent.ShowMessage(UiText.Res(R.string.scan_product_not_found))),
                appEvents,
            )
            assertEquals(FakeAppLogger.Level.Warn, logger.entries.single().level)
            assertTrue(errorReporter.reports.isEmpty())
            assertTrue(screenEvents.isEmpty())
            assertFalse(viewModel.state.value.showProgress)
            assertEquals(preview(isPaused = true), viewModel.state.value.camera)

            advanceTimeBy(1_999)
            assertEquals(preview(isPaused = true), viewModel.state.value.camera)

            advanceTimeBy(1)
            runCurrent()
            assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        }

    @Test
    fun `when search fails with parsing then the error message is shown and reported once`() = runTest {
        products.result = Outcome.Failure(AppError.Parsing)
        val viewModel = createViewModel(initialQuery = "590")

        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertEquals(AppEvent.ShowMessage(AppError.Parsing.toUiText()), appEvents.last())
        assertEquals(AppError.Parsing, errorReporter.reports.single().error)
        assertTrue(logger.entries.isEmpty())
    }

    @Test
    fun `when a barcode is scanned while scanning is paused then it is ignored`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))

        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when started with a torch then torch availability reaches the state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = true))

        assertEquals(preview(torch = Torch.Off), viewModel.state.value.camera)
    }

    @Test
    fun `when torch is clicked then it toggles and is saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = true))

        viewModel.onAction(ScanAction.OnTorchClicked)
        assertEquals(preview(torch = Torch.On), viewModel.state.value.camera)
        assertEquals(true, handle.get<Boolean>(KEY_TORCH))

        viewModel.onAction(ScanAction.OnTorchClicked)
        assertEquals(preview(torch = Torch.Off), viewModel.state.value.camera)
        assertEquals(false, handle.get<Boolean>(KEY_TORCH))
    }

    @Test
    fun `when handle has torch and type sheet state then both are restored`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_TORCH to true, KEY_TYPE_SHEET_OPEN to true)))

        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = true))

        assertEquals(preview(isPaused = true, torch = Torch.On, isTypeSheetOpen = true), viewModel.state.value.camera)
    }

    @Test
    fun `when type it is clicked and the sheet is dismissed then it opens and closes and is saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnTypeItClicked)
        assertEquals(preview(isPaused = true, isTypeSheetOpen = true), viewModel.state.value.camera)
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

        assertTrue(screenEvents.isEmpty())
        assertEquals(false, handle.get<Boolean>(KEY_TYPE_SHEET_OPEN))
        assertEquals(ratedCard, viewModel.state.value.found)
        assertEquals(preview(isPaused = true), viewModel.state.value.camera)
    }

    @Test
    fun `when a typed lookup fails then the sheet stays open`() = runTest {
        products.result = Outcome.Failure(AppError.NotFound)
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle, initialQuery = "590")
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnTypeItClicked)

        viewModel.onAction(ScanAction.OnSearchSubmitted)
        advanceTimeBy(2_000)
        runCurrent()

        assertEquals(AppEvent.ShowMessage(UiText.Res(R.string.scan_product_not_found)), appEvents.last())
        assertEquals(preview(isPaused = true, isTypeSheetOpen = true), viewModel.state.value.camera)
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
            assertTrue(viewModel.state.value.showProgress)
            assertNull(viewModel.state.value.found)
            assertEquals(preview(isPaused = true), viewModel.state.value.camera)
            assertTrue(history.saved.isEmpty())

            analyzeProduct.emit(ratedAnalysis)

            assertFalse(viewModel.state.value.showProgress)
            assertEquals(ratedCard, viewModel.state.value.found)
            assertEquals(preview(isPaused = true), viewModel.state.value.camera)
            assertEquals(listOf<ProductAnalysis>(ratedAnalysis), history.saved)

            analyzeProduct.emit(ratedAnalysis.copy(product = product.copy(name = "Nutella B-ready")))

            assertEquals(1, history.saved.size)
        }

    @Test
    fun `when details is clicked then product opens the card stays and no further history is saved`() = runTest {
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnDetailsClicked)

        assertEquals(listOf<ScanEvent>(NavigateToProduct("590")), screenEvents)
        assertEquals(ratedCard, viewModel.state.value.found)
        assertEquals(1, history.saved.size)
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
        assertFalse(viewModel.state.value.showProgress)
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
            assertEquals(preview(isPaused = true), viewModel.state.value.camera)
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
            assertFalse(viewModel.state.value.showProgress)
            assertEquals(
                listOf(AppEvent.HideKeyboard, AppEvent.ShowMessage(AppError.Storage.toUiText())),
                appEvents,
            )
            assertEquals(AppError.Storage, errorReporter.reports.single().error)
            assertTrue(history.saved.isEmpty())
            assertEquals(preview(isPaused = true), viewModel.state.value.camera)

            advanceTimeBy(2_000)
            runCurrent()
            assertEquals(preview(isPaused = false), viewModel.state.value.camera)
        }

    @Test
    fun `when the history save fails then it is only logged and the card stays`() = runTest {
        history.result = Outcome.Failure(AppError.Storage)
        analyzeProduct.emit(ratedAnalysis)
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED, hasTorch = false))

        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        assertEquals(ratedCard, viewModel.state.value.found)
        assertEquals(FakeAppLogger.Level.Error, logger.entries.single().level)
        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEvents)
        assertTrue(errorReporter.reports.isEmpty())
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
                    brand = UiText.Raw("Ferrero"),
                    rating = Rating.Unknown,
                    title = UiText.Res(R.string.scan_verdict_insufficient_data),
                    detail = UiText.Res(R.string.scan_found_insufficient_detail),
                ),
                viewModel.state.value.found,
            )
        }

    private fun preview(isPaused: Boolean = false, torch: Torch = Torch.Unavailable, isTypeSheetOpen: Boolean = false) =
        CameraState.Preview(isPaused = isPaused, torch = torch, isTypeSheetOpen = isTypeSheetOpen)

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
            logger = logger,
            errorReporter = errorReporter,
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
        val saved = mutableListOf<ProductAnalysis>()

        override suspend fun save(analysis: ProductAnalysis): Outcome<Unit> {
            saved += analysis
            return result
        }

        override suspend fun getLastUniqueEntries(amount: Int): Outcome<List<ProductAnalysisHistoryEntry>> =
            throw NotImplementedError()

        override fun observeLastUniqueEntries(amount: Int): Flow<Outcome<List<ProductAnalysisHistoryEntry>>> =
            throw NotImplementedError()
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_CAMERA_PERMISSION = "cameraPermission"
        const val KEY_TORCH = "torch"
        const val KEY_TYPE_SHEET_OPEN = "typeSheetOpen"
        const val KEY_FOUND_BARCODE = "foundBarcode"

        val product = Product(
            name = "Nutella",
            barcode = "590",
            brand = "Ferrero",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, 57.5, null, null),
            imageUrl = null,
            servingAmount = null,
        )
        val ratedAnalysis = ProductAnalysis.Rated(
            product = product,
            macronutrients = MacronutrientAnalysisResult(
                DietMode.KETO,
                DietRate.NOT_ADVISED,
                emptyList(),
                57.5,
                69.6,
                40,
            ),
        )
        val ratedCard = FoundCardState(
            name = UiText.Raw("Nutella"),
            brand = UiText.Raw("Ferrero"),
            rating = Rating.Negative,
            title = UiText.Res(R.string.scan_verdict_not_advised_keto),
            detail = UiText.Res(R.string.scan_found_carbs_per_100g, UiArg.Numeric.Grams(57.5)),
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
