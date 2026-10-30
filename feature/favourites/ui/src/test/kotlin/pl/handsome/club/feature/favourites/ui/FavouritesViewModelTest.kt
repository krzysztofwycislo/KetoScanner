package pl.handsome.club.feature.favourites.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.testing.FakeErrorReporter
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.favourites.api.FavouriteProductsRepository
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction
import pl.handsome.club.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation.NavigateToProduct
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState
import pl.handsome.club.feature.favourites.ui.state.FavouritesStateFactory
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.model.Product
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class FavouritesViewModelTest {

    private val favourites = FakeFavourites()
    private val preferences = FakePreferences()
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
    fun `when created then favourites for an empty query are shown`() = runTest {
        favourites.result = Outcome.Success(listOf(favourite("1")))
        val viewModel = createViewModel()

        val items = assertIs<FavouritesContentState.ShowItems>(viewModel.state.value.content).items
        assertEquals(listOf("1"), items.map { it.barcode })
        assertEquals(listOf(""), favourites.queries)
    }

    @Test
    fun `when handle was restored with a query then it is shown and used for the first subscription`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_QUERY to " cola ")))

        assertEquals(" cola ", viewModel.state.value.query)
        assertEquals(listOf("cola"), favourites.queries)
    }

    @Test
    fun `when query changes then it is saved and favourites are observed with the trimmed query`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(FavouritesAction.OnQueryChanged("  butter "))

        assertEquals("  butter ", viewModel.state.value.query)
        assertEquals("  butter ", handle.get<String>(KEY_QUERY))
        assertEquals(listOf("", "butter"), favourites.queries)
    }

    @Test
    fun `when query changes and its list is not loaded yet then the empty message stays for the previous query`() =
        runTest {
            val viewModel = createViewModel()
            favourites.pendingQueries += "cola"

            viewModel.onAction(FavouritesAction.OnQueryChanged("cola"))

            assertEquals(FavouritesContentState.Empty, viewModel.state.value.content)
        }

    @Test
    fun `when query changes only by whitespace then favourites are not observed again`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(FavouritesAction.OnQueryChanged("  "))

        assertEquals(listOf(""), favourites.queries)
    }

    @Test
    fun `when loading fails then error is shown and reported once`() = runTest {
        favourites.result = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()

        assertIs<FavouritesContentState.Error>(viewModel.state.value.content)
        assertEquals(listOf(AppError.Storage), errorReporter.reports.map { it.error })
    }

    @Test
    fun `when retry is clicked after a failure then favourites are observed again and shown`() = runTest {
        favourites.result = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()

        favourites.result = Outcome.Success(listOf(favourite("1")))
        viewModel.onAction(FavouritesAction.OnRetryClicked)

        assertIs<FavouritesContentState.ShowItems>(viewModel.state.value.content)
        assertEquals(listOf("", ""), favourites.queries)
        assertEquals(1, errorReporter.reports.size)
    }

    @Test
    fun `when item is clicked then keyboard is hidden and navigate to product is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(FavouritesAction.OnItemClicked("123"))

        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEventsReceived)
        assertEquals(NavigateToProduct("123"), viewModel.events.first())
    }

    @Test
    fun `when search is submitted then keyboard is hidden`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(FavouritesAction.OnSearchSubmitted)

        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEventsReceived)
    }

    @Test
    fun `when low carb preferences load then item verdicts follow the diet mode`() = runTest {
        favourites.result = Outcome.Success(listOf(favourite("1", DietRate.GOOD)))
        val viewModel = createViewModel()

        preferences.emit(Outcome.Success(DietPreferences(80, DietMode.LOW_CARB)))

        val item = assertIs<FavouritesContentState.ShowItems>(viewModel.state.value.content).items.single()
        assertEquals(UiText.Res(R.string.favourites_verdict_good_low_carb), item.verdictDescription)
    }

    @Test
    fun `when preferences fail to load then failure is reported and content stays`() = runTest {
        favourites.result = Outcome.Success(listOf(favourite("1")))
        val viewModel = createViewModel()

        preferences.emit(Outcome.Failure(AppError.Storage))

        assertIs<FavouritesContentState.ShowItems>(viewModel.state.value.content)
        assertEquals(listOf(AppError.Storage), errorReporter.reports.map { it.error })
    }

    private fun TestScope.createViewModel(handle: SavedStateHandle = SavedStateHandle()): FavouritesViewModel {
        val appEvents = AppEvents(backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { appEvents.events.toList(appEventsReceived) }
        return FavouritesViewModel(
            savedStateHandle = handle,
            favourites = favourites,
            preferences = preferences,
            appEvents = appEvents,
            stateFactory = FavouritesStateFactory(FavouritesResources()),
            errorReporter = errorReporter,
        )
    }

    private class FakeFavourites : FavouriteProductsRepository {

        var result: Outcome<List<FavouriteProduct>> = Outcome.Success(emptyList())
        val queries = mutableListOf<String>()
        val pendingQueries = mutableSetOf<String>()

        override fun observeFavourites(nameQuery: String): Flow<Outcome<List<FavouriteProduct>>> = flow {
            queries += nameQuery
            if (nameQuery in pendingQueries) awaitCancellation()
            emit(result)
        }

        override suspend fun addToFavourites(analysis: ProductAnalysis): Outcome<Unit> = Outcome.Success(Unit)

        override suspend fun findByBarcode(barcode: String): Outcome<FavouriteProduct?> = Outcome.Success(null)

        override suspend fun removeFromFavourites(product: Product): Outcome<Unit> = Outcome.Success(Unit)

        override suspend fun updateRating(analysis: ProductAnalysis): Outcome<Unit> = Outcome.Success(Unit)
    }

    private class FakePreferences : DietPreferencesRepository {

        private val flow = MutableSharedFlow<Outcome<DietPreferences>>()

        suspend fun emit(outcome: Outcome<DietPreferences>) = flow.emit(outcome)

        override fun observeDietPreferences(): Flow<Outcome<DietPreferences>> = flow

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = throw NotImplementedError()

        override suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit> =
            throw NotImplementedError()
    }

    private companion object {
        const val KEY_QUERY = "query"

        fun favourite(barcode: String, dietRate: DietRate? = null) =
            FavouriteProduct(
                barcode,
                "Name $barcode",
                "Brand",
                Instant.fromEpochMilliseconds(1_700_000_000_000),
                null,
                dietRate,
                null,
            )
    }
}
