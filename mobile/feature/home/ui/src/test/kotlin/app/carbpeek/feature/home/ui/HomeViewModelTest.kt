package app.carbpeek.feature.home.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.getOrThrow
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.core.testing.FakeClock
import app.carbpeek.core.testing.FakeErrorReporter
import app.carbpeek.feature.history.api.AnalysisHistoryRepository
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.home.ui.event.HomeAction
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToAbout
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToDietPreferences
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToHistory
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToProduct
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToScan
import app.carbpeek.feature.home.ui.state.HomeRecentState
import app.carbpeek.feature.home.ui.state.HomeStateFactory
import app.carbpeek.feature.home.ui.state.HomeStatePrototype
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.product.api.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class HomeViewModelTest {

    private val history = FakeHistory()
    private val preferences = FakePreferences()
    private val clock = FakeClock(NOW)
    private val errorReporter = FakeErrorReporter()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when created then recent is initial load and the recent limit is requested`() = runTest {
        val viewModel = createViewModel()

        assertEquals(HomeRecentState.InitialLoad, viewModel.state.value.recent)
        assertEquals(listOf(HomeStatePrototype.RECENT_LIMIT), history.requestedAmounts)
        assertEquals(4, HomeStatePrototype.RECENT_LIMIT)
    }

    @Test
    fun `when entries are emitted then items are shown`() = runTest {
        val viewModel = createViewModel()

        history.emit(Outcome.Success(entries))

        val recent = assertIs<HomeRecentState.ShowItems>(viewModel.state.value.recent)
        assertEquals(entries.map { it.id }, recent.items.map { it.id })
    }

    @Test
    fun `when a new emission arrives then the list is updated`() = runTest {
        val viewModel = createViewModel()
        history.emit(Outcome.Success(entries))

        history.emit(Outcome.Success(listOf(entry(9)) + entries))

        val recent = assertIs<HomeRecentState.ShowItems>(viewModel.state.value.recent)
        assertEquals(listOf(9L, 2L, 1L), recent.items.map { it.id })
    }

    @Test
    fun `when first emission fails then error is shown and reported once`() = runTest {
        val viewModel = createViewModel()

        history.emit(Outcome.Failure(AppError.Storage))

        assertIs<HomeRecentState.Error>(viewModel.state.value.recent)
        assertEquals(1, errorReporter.reports.size)
        assertEquals(AppError.Storage, errorReporter.reports.single().error)
    }

    @Test
    fun `when retry is clicked after a failure then it resubscribes and shows items`() = runTest {
        val viewModel = createViewModel()
        history.emit(Outcome.Failure(AppError.Storage))

        viewModel.onAction(HomeAction.OnRetryClicked)

        assertEquals(HomeRecentState.InitialLoad, viewModel.state.value.recent)
        assertEquals(2, history.requestedAmounts.size)
        history.emit(Outcome.Success(entries))
        assertIs<HomeRecentState.ShowItems>(viewModel.state.value.recent)
    }

    @Test
    fun `when failure arrives after items were shown then items stay and failure is reported`() = runTest {
        val viewModel = createViewModel()
        history.emit(Outcome.Success(entries))

        history.emit(Outcome.Failure(AppError.Storage))

        assertIs<HomeRecentState.ShowItems>(viewModel.state.value.recent)
        assertEquals(1, errorReporter.reports.size)
        assertEquals(1, history.requestedAmounts.size)

        advanceTimeBy(HomeViewModel.RETRY_DELAY + 1.milliseconds)

        assertEquals(2, history.requestedAmounts.size)
        history.emit(Outcome.Success(listOf(entry(9)) + entries))
        val recent = assertIs<HomeRecentState.ShowItems>(viewModel.state.value.recent)
        assertEquals(listOf(9L, 2L, 1L), recent.items.map { it.id })
    }

    @Test
    fun `when preferences load then profile label shows them`() = runTest {
        val viewModel = createViewModel()

        preferences.emit(Outcome.Success(DietPreferences(40, DietMode.KETO)))

        assertEquals(
            HomeResources().profileLabel(DietPreferences(40, DietMode.KETO)),
            viewModel.state.value.profileLabel,
        )
    }

    @Test
    fun `when preferences fail then failure is only reported and label stays default`() = runTest {
        val viewModel = createViewModel()

        preferences.emit(Outcome.Failure(AppError.Storage))

        assertEquals(UiText.Res(R.string.home_profile_default), viewModel.state.value.profileLabel)
        assertEquals(1, errorReporter.reports.size)
        assertEquals(HomeRecentState.InitialLoad, viewModel.state.value.recent)
    }

    @Test
    fun `when started after the clock moved a day then today entries become yesterday`() = runTest {
        val viewModel = createViewModel()
        history.emit(Outcome.Success(listOf(entry(1))))
        val before = assertIs<HomeRecentState.ShowItems>(viewModel.state.value.recent).items.single()
        assertEquals(HomeResources().today(NOW), before.time)

        clock.now = NOW + 1.days
        viewModel.onAction(HomeAction.OnStart)

        val after = assertIs<HomeRecentState.ShowItems>(viewModel.state.value.recent).items.single()
        assertEquals(HomeResources().yesterday(), after.time)
    }

    @Test
    fun `when click actions happen then matching navigation events are emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HomeAction.OnProfileClicked)
        viewModel.onAction(HomeAction.OnAboutClicked)
        viewModel.onAction(HomeAction.OnStartScanningClicked)
        viewModel.onAction(HomeAction.OnSeeAllClicked)
        viewModel.onAction(HomeAction.OnEntryClicked("123"))

        assertEquals(
            listOf(
                NavigateToDietPreferences,
                NavigateToAbout,
                NavigateToScan,
                NavigateToHistory,
                NavigateToProduct("123"),
            ),
            viewModel.events.take(5).toList(),
        )
    }

    private fun createViewModel() = HomeViewModel(
        history = history,
        preferences = preferences,
        stateFactory = HomeStateFactory(HomeResources()),
        clock = clock,
        timeZone = TimeZone.UTC,
        errorReporter = errorReporter,
    )

    private class FakeHistory : AnalysisHistoryRepository {

        private val subscriptions = mutableListOf<MutableSharedFlow<Outcome<List<ProductAnalysisHistoryEntry>>>>()
        val requestedAmounts = mutableListOf<Int>()

        suspend fun emit(outcome: Outcome<List<ProductAnalysisHistoryEntry>>) = subscriptions.last().emit(outcome)

        override suspend fun save(product: Product): Outcome<Unit> = Outcome.Success(Unit)

        override fun observeLastUniqueEntries(amount: Int): Flow<List<ProductAnalysisHistoryEntry>> = flow {
            requestedAmounts += amount
            val subscription = MutableSharedFlow<Outcome<List<ProductAnalysisHistoryEntry>>>()
            subscriptions += subscription
            emitAll(subscription.map { it.getOrThrow() })
        }
    }

    private class FakePreferences : DietPreferencesRepository {

        private val flow = MutableSharedFlow<Outcome<DietPreferences>>()

        suspend fun emit(outcome: Outcome<DietPreferences>) = flow.emit(outcome)

        override fun observeDietPreferences(): Flow<DietPreferences> = flow.map { it.getOrThrow() }

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = throw NotImplementedError()
    }

    private companion object {
        val NOW = Instant.parse("2026-10-12T18:42:00Z")

        fun entry(id: Long) = ProductAnalysisHistoryEntry(
            id = id,
            productName = "Name $id",
            productBarcode = "barcode-$id",
            productBrand = "Brand",
            updateTime = NOW,
            dietRate = null,
            carbsPer100g = null,
            category = ProductCategory.OTHER,
        )

        val entries = listOf(entry(2), entry(1))
    }
}
