package pl.morning.tower.karbo.feature.report.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.designsystem.event.AppEvent
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.core.testing.FakeAppLogger
import pl.morning.tower.karbo.core.testing.FakeErrorReporter
import pl.morning.tower.karbo.feature.analysis.api.AnalyzeProduct
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.product.api.model.Product
import pl.morning.tower.karbo.feature.product.api.model.ProductNutriments
import pl.morning.tower.karbo.feature.report.api.ReportRepository
import pl.morning.tower.karbo.feature.report.api.model.IssueReason
import pl.morning.tower.karbo.feature.report.api.model.IssueReport
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportAction
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.ReportNavigation.NavigateBack
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.ReportNavigation.NavigateToHome
import pl.morning.tower.karbo.feature.report.ui.event.IssueReportEvent.ReportNavigation.NavigateToScan
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportContentState
import pl.morning.tower.karbo.feature.report.ui.state.IssueReportStateFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IssueReportViewModelTest {

    private val analyzeProduct = FakeAnalyzeProduct()
    private val dietPreferences = FakeDietPreferences()
    private val reports = FakeReportRepository()
    private val logger = FakeAppLogger()
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
            assertEquals(1, reports.submitted.size)

            gate.complete(Unit)
            assertEquals(IssueReportContentState.Sent, viewModel.state.value.content)
        }

    @Test
    fun `when send is clicked without a reason then nothing is sent`() = runTest {
        val viewModel = createViewModel()
        collectAppEvents()

        viewModel.onAction(IssueReportAction.OnSendClicked)

        assertTrue(reports.submitted.isEmpty())
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
                ),
                reports.submitted.single(),
            )
        }

    @Test
    fun `when the reason changes away from rating then the expected rate is not sent`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.RATING))
        viewModel.onAction(IssueReportAction.OnExpectedRateClicked(DietRate.GOOD))
        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.NUTRITION))
        viewModel.onAction(IssueReportAction.OnSendClicked)

        val report = reports.submitted.single()
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
        assertTrue(logger.entries.isEmpty())
    }

    @Test
    fun `when sending fails without network then only a warning is logged`() = runTest {
        reports.result = Outcome.Failure(AppError.Network)
        val viewModel = createViewModel()

        viewModel.onAction(IssueReportAction.OnReasonClicked(IssueReason.OTHER))
        viewModel.onAction(IssueReportAction.OnSendClicked)

        assertEquals(IssueReportContentState.Failed, viewModel.state.value.content)
        assertEquals(FakeAppLogger.Level.Warn, logger.entries.single().level)
        assertTrue(errorReporter.reports.isEmpty())
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

        assertEquals(2, reports.submitted.size)
        assertEquals(reports.submitted[0], reports.submitted[1])
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

        assertTrue(reports.submitted.isEmpty())
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

    private fun TestScope.createViewModel(handle: SavedStateHandle = handle()): IssueReportViewModel =
        IssueReportViewModel(
            route = ReportRoute(BARCODE),
            savedStateHandle = handle,
            analyzeProduct = analyzeProduct,
            dietPreferences = dietPreferences,
            reports = reports,
            appEvents = appEvents(),
            stateFactory = IssueReportStateFactory(IssueReportResources()),
            logger = logger,
            errorReporter = errorReporter,
        )

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

        val preferences = MutableStateFlow(DietPreferences.Default)

        override fun observeDietPreferences(): Flow<DietPreferences> = preferences

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = Outcome.Success(preferences.value)

        override suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit> =
            throw NotImplementedError()
    }

    private class FakeReportRepository : ReportRepository {

        var result: Outcome<Unit> = Outcome.Success(Unit)
        val submitted = mutableListOf<IssueReport>()
        private var gate: CompletableDeferred<Unit>? = null

        fun gate(): CompletableDeferred<Unit> = CompletableDeferred<Unit>().also { gate = it }

        override suspend fun submitIssue(report: IssueReport): Outcome<Unit> {
            submitted += report
            gate?.await()
            return result
        }
    }

    private companion object {
        const val BARCODE = "5449000000996"
        const val PRODUCT_NAME = "Coca-Cola Original Taste"
        const val KEY_REASON = "reason"
        const val KEY_DETAILS = "details"
        const val KEY_EXPECTED_RATE = "expectedRate"
        const val KEY_STATUS = "status"

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
