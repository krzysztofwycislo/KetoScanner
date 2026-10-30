package pl.handsome.club.feature.home.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.testing.FakeClock
import pl.handsome.club.core.testing.FakeErrorReporter
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.home.ui.event.HomeAction
import pl.handsome.club.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToAbout
import pl.handsome.club.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToDietPreferences
import pl.handsome.club.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToHistory
import pl.handsome.club.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToProduct
import pl.handsome.club.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToScan
import pl.handsome.club.feature.home.ui.state.HomeRecentState
import pl.handsome.club.feature.home.ui.state.HomeStateFactory
import pl.handsome.club.feature.home.ui.state.HomeStatePrototype
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.days
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

        override suspend fun save(analysis: ProductAnalysis): Outcome<Unit> = Outcome.Success(Unit)

        override suspend fun getLastUniqueEntries(amount: Int): Outcome<List<ProductAnalysisHistoryEntry>> =
            throw NotImplementedError()

        override fun observeLastUniqueEntries(amount: Int): Flow<Outcome<List<ProductAnalysisHistoryEntry>>> {
            requestedAmounts += amount
            return MutableSharedFlow<Outcome<List<ProductAnalysisHistoryEntry>>>().also { subscriptions += it }
        }
    }

    private class FakePreferences : DietPreferencesRepository {

        private val flow = MutableSharedFlow<Outcome<DietPreferences>>()

        suspend fun emit(outcome: Outcome<DietPreferences>) = flow.emit(outcome)

        override fun observeDietPreferences(): Flow<Outcome<DietPreferences>> = flow

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = throw NotImplementedError()

        override suspend fun setMaxDailyCarbsAmount(amount: Int): Outcome<Unit> = throw NotImplementedError()

        override suspend fun setDietMode(mode: DietMode): Outcome<Unit> = throw NotImplementedError()
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
        )

        val entries = listOf(entry(2), entry(1))
    }
}
