package pl.handsome.club.feature.scanner.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.testing.FakeAppLogger
import pl.handsome.club.core.testing.FakeErrorReporter
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.scanner.ui.event.OpenAppSettings
import pl.handsome.club.feature.scanner.ui.event.RequestCameraPermission
import pl.handsome.club.feature.scanner.ui.event.ScanAction
import pl.handsome.club.feature.scanner.ui.event.ScanEvent
import pl.handsome.club.feature.scanner.ui.event.ScanEvent.ScanNavigation.NavigateToProduct
import pl.handsome.club.feature.scanner.ui.state.CameraPermissionCheck
import pl.handsome.club.feature.scanner.ui.state.CameraState
import pl.handsome.club.feature.scanner.ui.state.ScanStateFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScanViewModelTest {

    private val products = FakeProductRepository()
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

        viewModel.onAction(ScanAction.OnStart(GRANTED))

        assertEquals(CameraState.Preview(isPaused = false), viewModel.state.value.camera)
        assertEquals("Granted", handle.get<String>(KEY_CAMERA_PERMISSION))
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when started with rationale then rationale panel is shown without a request`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ScanAction.OnStart(RATIONALE))

        assertEquals(rationalePanel, viewModel.state.value.camera)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when started for the first time without permission then it is requested once`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ScanAction.OnStart(NOT_GRANTED))

        assertEquals(listOf<ScanEvent>(RequestCameraPermission), screenEvents)
        assertEquals("Requesting", handle.get<String>(KEY_CAMERA_PERMISSION))
        assertEquals(CameraState.Hidden, viewModel.state.value.camera)
    }

    @Test
    fun `when restored as granted then camera stays hidden until the live check grants it`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_CAMERA_PERMISSION to "Granted")))

        assertEquals(CameraState.Hidden, viewModel.state.value.camera)

        viewModel.onAction(ScanAction.OnStart(GRANTED))

        assertEquals(CameraState.Preview(isPaused = false), viewModel.state.value.camera)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when restored after a permanent denial then denied panel is shown without a request`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_CAMERA_PERMISSION to "Denied")))

        viewModel.onAction(ScanAction.OnStart(NOT_GRANTED))

        assertEquals(deniedPanel, viewModel.state.value.camera)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when restored while a request is in flight then it is not requested again`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_CAMERA_PERMISSION to "Requesting")))

        viewModel.onAction(ScanAction.OnStart(NOT_GRANTED))

        assertTrue(screenEvents.isEmpty())
        assertEquals(CameraState.Hidden, viewModel.state.value.camera)
    }

    @Test
    fun `when permission result arrives then it maps to granted rationale or denied and is stored`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(ScanAction.OnCameraPermissionResult(GRANTED))
        assertEquals(CameraState.Preview(isPaused = false), viewModel.state.value.camera)
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

        viewModel.onAction(ScanAction.OnStart(GRANTED))

        assertEquals(CameraState.Preview(isPaused = false), viewModel.state.value.camera)
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when permission button is clicked in rationale then permission is requested`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(RATIONALE))

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
    fun `when a search is submitted then keyboard hides trimmed barcode is refreshed and product opens`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(ScanAction.OnStart(GRANTED))

        viewModel.onAction(ScanAction.OnQueryChanged("  590  "))
        viewModel.onAction(ScanAction.OnSearchSubmitted)

        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEvents)
        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertEquals(listOf<ScanEvent>(NavigateToProduct("590")), screenEvents)
        assertEquals(CameraState.Preview(isPaused = true), viewModel.state.value.camera)

        viewModel.onAction(ScanAction.OnStart(GRANTED))

        assertEquals(CameraState.Preview(isPaused = false), viewModel.state.value.camera)
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
        viewModel.onAction(ScanAction.OnStart(GRANTED))

        viewModel.onAction(ScanAction.OnSearchSubmitted)
        assertTrue(viewModel.state.value.showProgress)
        viewModel.onAction(ScanAction.OnSearchSubmitted)
        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))
        gate.complete(Unit)

        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertFalse(viewModel.state.value.showProgress)
        assertEquals(listOf<ScanEvent>(NavigateToProduct("590")), screenEvents)
    }

    @Test
    fun `when a typed search is pending and the screen starts again then camera stays paused`() = runTest {
        val gate = products.gate()
        val viewModel = createViewModel(initialQuery = "590")
        viewModel.onAction(ScanAction.OnStart(GRANTED))
        viewModel.onAction(ScanAction.OnSearchSubmitted)

        viewModel.onAction(ScanAction.OnStart(GRANTED))

        assertEquals(CameraState.Preview(isPaused = true), viewModel.state.value.camera)
        gate.complete(Unit)
    }

    @Test
    fun `when a scanned product is not found then message is shown warning logged and scan resumes after 2 seconds`() =
        runTest {
            products.result = Outcome.Failure(AppError.NotFound)
            val viewModel = createViewModel()
            viewModel.onAction(ScanAction.OnStart(GRANTED))

            viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

            assertEquals(
                listOf(AppEvent.HideKeyboard, AppEvent.ShowMessage(UiText.Res(R.string.scan_product_not_found))),
                appEvents,
            )
            assertEquals(FakeAppLogger.Level.Warn, logger.entries.single().level)
            assertTrue(errorReporter.reports.isEmpty())
            assertTrue(screenEvents.isEmpty())
            assertFalse(viewModel.state.value.showProgress)
            assertEquals(CameraState.Preview(isPaused = true), viewModel.state.value.camera)

            advanceTimeBy(1_999)
            assertEquals(CameraState.Preview(isPaused = true), viewModel.state.value.camera)

            advanceTimeBy(1)
            runCurrent()
            assertEquals(CameraState.Preview(isPaused = false), viewModel.state.value.camera)
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
        viewModel.onAction(ScanAction.OnStart(GRANTED))
        viewModel.onAction(ScanAction.OnBarcodeScanned("590"))

        viewModel.onAction(ScanAction.OnBarcodeScanned("123"))

        assertEquals(listOf("590"), products.refreshedBarcodes)
        assertEquals(listOf<ScanEvent>(NavigateToProduct("590")), screenEvents)
    }

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
            appEvents = events,
            stateFactory = ScanStateFactory(resources),
            resources = resources,
            logger = logger,
            errorReporter = errorReporter,
            initialQuery = initialQuery,
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.toList(screenEvents) }
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

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_CAMERA_PERMISSION = "cameraPermission"

        val GRANTED = CameraPermissionCheck(isGranted = true, shouldShowRationale = false)
        val RATIONALE = CameraPermissionCheck(isGranted = false, shouldShowRationale = true)
        val NOT_GRANTED = CameraPermissionCheck(isGranted = false, shouldShowRationale = false)

        val rationalePanel = CameraState.PermissionPanel(
            message = UiText.Res(R.string.scan_camera_permission_rationale),
            buttonLabel = UiText.Res(R.string.scan_camera_permission_allow),
        )
        val deniedPanel = CameraState.PermissionPanel(
            message = UiText.Res(R.string.scan_camera_permission_denied),
            buttonLabel = UiText.Res(R.string.scan_camera_permission_open_settings),
        )
    }
}
