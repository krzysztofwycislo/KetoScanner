package pl.handsome.club.feature.history.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.testing.FakeAppLogger
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.history.ui.event.HistoryAction
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToAbout
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToDietPreferences
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToProduct
import pl.handsome.club.feature.history.ui.state.HistoryContentState
import pl.handsome.club.feature.history.ui.state.HistoryStateFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant

class HistoryViewModelTest {

    private val repository = FakeRepository()
    private val logger = FakeAppLogger()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when created then content is initial load`() = runTest {
        val viewModel = createViewModel()

        assertEquals(HistoryContentState.InitialLoad, viewModel.state.value.content)
    }

    @Test
    fun `when started and load succeeds then items are shown and limit is requested`() = runTest {
        repository.result = Outcome.Success(entries)
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnStart)

        val content = assertIs<HistoryContentState.ShowItems>(viewModel.state.value.content)
        assertEquals(entries.map { it.id }, content.items.map { it.id })
        assertEquals(listOf(50), repository.requestedAmounts)
    }

    @Test
    fun `when first load fails then error is shown and logged once`() = runTest {
        repository.result = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnStart)

        assertIs<HistoryContentState.Error>(viewModel.state.value.content)
        assertEquals(1, logger.entries.size)
        assertEquals(FakeAppLogger.Level.Error, logger.entries.single().level)
    }

    @Test
    fun `when retry succeeds after an error then items are shown`() = runTest {
        repository.result = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()
        viewModel.onAction(HistoryAction.OnStart)

        repository.result = Outcome.Success(entries)
        viewModel.onAction(HistoryAction.OnRetryClicked)

        assertIs<HistoryContentState.ShowItems>(viewModel.state.value.content)
    }

    @Test
    fun `when refresh fails after items were shown then items stay and failure is logged`() = runTest {
        repository.result = Outcome.Success(entries)
        val viewModel = createViewModel()
        viewModel.onAction(HistoryAction.OnStart)

        repository.result = Outcome.Failure(AppError.Storage)
        viewModel.onAction(HistoryAction.OnStart)

        assertIs<HistoryContentState.ShowItems>(viewModel.state.value.content)
        assertEquals(1, logger.entries.size)
    }

    @Test
    fun `when load fails with unknown error then cause is logged`() = runTest {
        val cause = IllegalStateException("boom")
        repository.result = Outcome.Failure(AppError.Unknown(cause))
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnStart)

        assertEquals(cause, logger.entries.single().cause)
    }

    @Test
    fun `when started while a load is suspended then repository is called once`() = runTest {
        val pending = CompletableDeferred<Outcome<List<ProductAnalysisHistoryEntry>>>()
        repository.pending = pending
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnStart)
        viewModel.onAction(HistoryAction.OnStart)
        pending.complete(Outcome.Success(entries))

        assertEquals(1, repository.requestedAmounts.size)
        assertIs<HistoryContentState.ShowItems>(viewModel.state.value.content)
    }

    @Test
    fun `when entry is clicked then navigate to product is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnEntryClicked("123"))

        assertEquals(NavigateToProduct("123"), viewModel.events.first())
    }

    @Test
    fun `when two entries are clicked before collecting then both events arrive in order`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HistoryAction.OnEntryClicked("1"))
        viewModel.onAction(HistoryAction.OnEntryClicked("2"))

        assertEquals(listOf(NavigateToProduct("1"), NavigateToProduct("2")), viewModel.events.take(2).toList())
    }

    @Test
    fun `when menu is clicked then it opens and the handle is written`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(HistoryAction.OnMenuClicked)

        assertTrue(viewModel.state.value.isMenuOpen)
        assertEquals(true, handle.get<Boolean>(KEY_MENU_OPEN))
    }

    @Test
    fun `when menu is dismissed then it closes and the handle is written`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(HistoryAction.OnMenuClicked)

        viewModel.onAction(HistoryAction.OnMenuDismissed)

        assertFalse(viewModel.state.value.isMenuOpen)
        assertEquals(false, handle.get<Boolean>(KEY_MENU_OPEN))
    }

    @Test
    fun `when handle was restored with an open menu then menu is open`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_MENU_OPEN to true)))

        assertTrue(viewModel.state.value.isMenuOpen)
    }

    @Test
    fun `when diet preferences is clicked then menu closes and navigate to diet preferences is emitted`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(HistoryAction.OnMenuClicked)

        viewModel.onAction(HistoryAction.OnDietPreferencesClicked)

        assertFalse(viewModel.state.value.isMenuOpen)
        assertEquals(false, handle.get<Boolean>(KEY_MENU_OPEN))
        assertEquals(NavigateToDietPreferences, viewModel.events.first())
    }

    @Test
    fun `when about is clicked then menu closes and navigate to about is emitted`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(HistoryAction.OnMenuClicked)

        viewModel.onAction(HistoryAction.OnAboutClicked)

        assertFalse(viewModel.state.value.isMenuOpen)
        assertEquals(false, handle.get<Boolean>(KEY_MENU_OPEN))
        assertEquals(NavigateToAbout, viewModel.events.first())
    }

    private fun createViewModel(handle: SavedStateHandle = SavedStateHandle()) =
        HistoryViewModel(handle, repository, HistoryStateFactory(HistoryResources()), logger)

    private class FakeRepository : AnalysisHistoryRepository {

        var result: Outcome<List<ProductAnalysisHistoryEntry>> = Outcome.Success(emptyList())
        var pending: CompletableDeferred<Outcome<List<ProductAnalysisHistoryEntry>>>? = null
        val requestedAmounts = mutableListOf<Int>()

        override suspend fun save(analysis: ProductAnalysis): Outcome<Unit> = Outcome.Success(Unit)

        override suspend fun getLastUniqueEntries(amount: Int): Outcome<List<ProductAnalysisHistoryEntry>> {
            requestedAmounts += amount
            return pending?.await() ?: result
        }
    }

    private companion object {
        const val KEY_MENU_OPEN = "menuOpen"

        val entries = listOf(2L, 1L).map { id ->
            ProductAnalysisHistoryEntry(
                id = id,
                productName = "Name $id",
                productBarcode = "barcode-$id",
                productBrand = "Brand $id",
                updateTime = Instant.fromEpochMilliseconds(1_700_000_000_000),
                macronutrientAnalysisResult = null,
            )
        }
    }
}
