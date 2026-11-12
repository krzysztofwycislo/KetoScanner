package pl.morning.tower.karbo.feature.history.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.getOrThrow
import pl.morning.tower.karbo.core.designsystem.event.AppEvent
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.core.testing.FakeClock
import pl.morning.tower.karbo.core.testing.FakeErrorReporter
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.history.api.AnalysisHistoryRepository
import pl.morning.tower.karbo.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction
import pl.morning.tower.karbo.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateBack
import pl.morning.tower.karbo.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToProduct
import pl.morning.tower.karbo.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToScan
import pl.morning.tower.karbo.feature.history.ui.state.HistoryContentState
import pl.morning.tower.karbo.feature.history.ui.state.HistoryFilter
import pl.morning.tower.karbo.feature.history.ui.state.HistoryStateFactory
import pl.morning.tower.karbo.feature.history.ui.state.HistoryStatePrototype
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.product.api.model.Product
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class HistoryViewModelTest {

    private val repository = FakeHistory()
    private val preferences = FakePreferences()
    private val clock = FakeClock(NOW)
    private val errorReporter = FakeErrorReporter()
    private val appEventsReceived = mutableListOf<AppEvent>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when created then content is initial load and the limit is requested once`() = runTest {
        val viewModel = createViewModel()

        assertEquals(HistoryContentState.InitialLoad, viewModel.state.value.content)
        assertEquals(listOf(HistoryStatePrototype.ENTRIES_LIMIT), repository.requestedAmounts)
        assertEquals(50, HistoryStatePrototype.ENTRIES_LIMIT)
    }

    @Test
    fun `when entries are emitted then groups are shown`() = runTest {
        val viewModel = createViewModel()

        repository.emit(Outcome.Success(entries))

        assertEquals(listOf(2L, 1L), ids(viewModel))
    }

    @Test
    fun `when a new emission arrives then the list is updated`() = runTest {
        val viewModel = createViewModel()
        repository.emit(Outcome.Success(entries))

        repository.emit(Outcome.Success(listOf(entry(9)) + entries))

        assertEquals(listOf(9L, 2L, 1L), ids(viewModel))
    }

    @Test
    fun `when the first emission fails then error is shown and reported once`() = runTest {
        val viewModel = createViewModel()

        repository.emit(Outcome.Failure(AppError.Storage))

        assertIs<HistoryContentState.Error>(viewModel.state.value.content)
        assertEquals(1, errorReporter.reports.size)
        assertEquals(AppError.Storage, errorReporter.reports.single().error)
    }

    @Test
    fun `when retry is clicked after a failure then it resubscribes and shows items`() = runTest {
        val viewModel = createViewModel()
        repository.emit(Outcome.Failure(AppError.Storage))

        viewModel.onAction(HistoryAction.OnRetryClicked)

        assertEquals(HistoryContentState.InitialLoad, viewModel.state.value.content)
        assertEquals(2, repository.requestedAmounts.size)
        repository.emit(Outcome.Success(entries))
        assertEquals(listOf(2L, 1L), ids(viewModel))
    }

    @Test
    fun `when a failure arrives after items then items stay and it resubscribes after the retry delay`() = runTest {
        val viewModel = createViewModel()
        repository.emit(Outcome.Success(entries))

        repository.emit(Outcome.Failure(AppError.Storage))

        assertEquals(listOf(2L, 1L), ids(viewModel))
        assertEquals(1, errorReporter.reports.size)
        assertEquals(1, repository.requestedAmounts.size)

        advanceTimeBy(HistoryViewModel.RETRY_DELAY + 1.milliseconds)

        assertEquals(2, repository.requestedAmounts.size)
        repository.emit(Outcome.Success(listOf(entry(9)) + entries))
        assertEquals(listOf(9L, 2L, 1L), ids(viewModel))
    }

    @Test
    fun `when the query changes then state and handle are updated`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        repository.emit(Outcome.Success(listOf(entry(1, name = "Cola"), entry(2, name = "Peanut butter"))))

        viewModel.onAction(HistoryAction.OnQueryChanged("cola"))

        assertEquals("cola", viewModel.state.value.query)
        assertEquals("cola", handle.get<String>(KEY_QUERY))
        assertEquals(listOf(1L), ids(viewModel))
    }

    @Test
    fun `when a filter is clicked then state and handle are updated`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(HistoryAction.OnFilterClicked(HistoryFilter.NOT_ADVISED))

        assertEquals(HistoryFilter.NOT_ADVISED, viewModel.state.value.filters.single { it.selected }.filter)
        assertEquals(HistoryFilter.NOT_ADVISED.name, handle.get<String>(KEY_FILTER))
    }

    @Test
    fun `when entries are re-rated then the selected filter shows the new matches`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_FILTER to HistoryFilter.GOOD.name)))
        repository.emit(Outcome.Success(listOf(entry(2, rate = DietRate.NEUTRAL), entry(1, rate = DietRate.GOOD))))
        assertEquals(listOf(1L), ids(viewModel))

        repository.emit(Outcome.Success(listOf(entry(2, rate = DietRate.GOOD), entry(1, rate = DietRate.NOT_ADVISED))))

        assertEquals(listOf(2L), ids(viewModel))
        assertEquals(1, repository.requestedAmounts.size)
    }

    @Test
    fun `when the handle is pre-filled then the view model starts with that query and filter`() = runTest {
        val handle = SavedStateHandle(mapOf(KEY_QUERY to "nut", KEY_FILTER to HistoryFilter.GOOD.name))

        val viewModel = createViewModel(handle)

        assertEquals("nut", viewModel.state.value.query)
        assertEquals(HistoryFilter.GOOD, viewModel.state.value.filters.single { it.selected }.filter)
    }

    @Test
    fun `when preferences load then chip labels follow the diet mode`() = runTest {
        val viewModel = createViewModel()

        preferences.emit(Outcome.Success(DietPreferences(80, DietMode.LOW_CARB)))

        assertEquals(
            HistoryResources().filterLabel(HistoryFilter.GOOD, DietMode.LOW_CARB),
            viewModel.state.value.filters[1].label,
        )
    }

    @Test
    fun `when started after the clock moved a day then today becomes yesterday`() = runTest {
        val viewModel = createViewModel()
        repository.emit(Outcome.Success(listOf(entry(1))))
        assertEquals(listOf("today"), groupKeys(viewModel))

        clock.now = NOW + 1.days
        viewModel.onAction(HistoryAction.OnStart)

        assertEquals(listOf("yesterday"), groupKeys(viewModel))
    }

    @Test
    fun `when search is submitted then keyboard is hidden`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnSearchSubmitted)

        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEventsReceived)
    }

    @Test
    fun `when an entry is clicked then keyboard is hidden and product navigation is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnEntryClicked("123"))

        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEventsReceived)
        assertEquals(NavigateToProduct("123"), viewModel.events.first())
    }

    @Test
    fun `when back is clicked then navigate back is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnBackClicked)

        assertEquals(NavigateBack, viewModel.events.first())
    }

    @Test
    fun `when start scanning is clicked then navigate to scan is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnStartScanningClicked)

        assertEquals(NavigateToScan, viewModel.events.first())
    }

    private fun TestScope.createViewModel(handle: SavedStateHandle = SavedStateHandle()): HistoryViewModel {
        val appEvents = AppEvents(backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { appEvents.events.toList(appEventsReceived) }
        return HistoryViewModel(
            savedStateHandle = handle,
            repository = repository,
            preferences = preferences,
            appEvents = appEvents,
            stateFactory = HistoryStateFactory(HistoryResources()),
            clock = clock,
            timeZone = TimeZone.UTC,
            errorReporter = errorReporter,
        )
    }

    private fun ids(viewModel: HistoryViewModel) =
        assertIs<HistoryContentState.ShowGroups>(viewModel.state.value.content).groups
            .flatMap { group -> group.items.map { it.id } }

    private fun groupKeys(viewModel: HistoryViewModel) =
        assertIs<HistoryContentState.ShowGroups>(viewModel.state.value.content).groups.map { it.key }

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

        override suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit> =
            throw NotImplementedError()
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_FILTER = "filter"
        val NOW = Instant.parse("2026-10-12T18:42:00Z")

        fun entry(id: Long, name: String = "Name $id", rate: DietRate? = null) = ProductAnalysisHistoryEntry(
            id = id,
            productName = name,
            productBarcode = "barcode-$id",
            productBrand = "Brand",
            updateTime = NOW,
            dietRate = rate,
            carbsPer100g = null,
            category = ProductCategory.OTHER,
        )

        val entries = listOf(entry(2), entry(1))
    }
}
