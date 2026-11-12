package pl.morning.tower.karbo.feature.favourites.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.getOrThrow
import pl.morning.tower.karbo.core.designsystem.event.AppEvent
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.core.testing.FakeErrorReporter
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.favourites.api.FavouriteProductsRepository
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.favourites.ui.event.FavouritesAction
import pl.morning.tower.karbo.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation.NavigateToProduct
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouriteGroupState
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouriteItemState
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouritesContentState
import pl.morning.tower.karbo.feature.favourites.ui.state.FavouritesStateFactory
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.product.api.model.Product
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
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

        val items = items(viewModel)
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

        assertIs<FavouritesContentState.ShowGroups>(viewModel.state.value.content)
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

        val item = items(viewModel).single()
        assertEquals(UiText.Res(R.string.favourites_verdict_good_low_carb), item.verdictDescription)
    }

    @Test
    fun `when preferences fail to load then failure is reported and content stays`() = runTest {
        favourites.result = Outcome.Success(listOf(favourite("1")))
        val viewModel = createViewModel()

        preferences.emit(Outcome.Failure(AppError.Storage))

        assertIs<FavouritesContentState.ShowGroups>(viewModel.state.value.content)
        assertEquals(listOf(AppError.Storage), errorReporter.reports.map { it.error })
    }

    @Test
    fun `when a group is clicked then it collapses and expands and the handle follows`() = runTest {
        favourites.result = Outcome.Success(listOf(favourite("1", category = ProductCategory.CHEESE)))
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(FavouritesAction.OnGroupClicked(ProductCategory.CHEESE))

        assertFalse(groups(viewModel).single().expanded)
        assertEquals(arrayListOf("CHEESE"), handle.get<ArrayList<String>>(KEY_COLLAPSED))

        viewModel.onAction(FavouritesAction.OnGroupClicked(ProductCategory.CHEESE))

        assertTrue(groups(viewModel).single().expanded)
        assertEquals(arrayListOf<String>(), handle.get<ArrayList<String>>(KEY_COLLAPSED))
    }

    @Test
    fun `when handle was restored with collapsed categories then those groups are collapsed`() = runTest {
        favourites.result = Outcome.Success(
            listOf(
                favourite("1", category = ProductCategory.CHEESE),
                favourite("2", category = ProductCategory.MEAT),
            ),
        )

        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_COLLAPSED to arrayListOf("MEAT", "UNKNOWN"))))

        assertEquals(listOf(true, false), groups(viewModel).map { it.expanded })
    }

    @Test
    fun `when a group is clicked while a search is applied then it is ignored`() = runTest {
        favourites.result = Outcome.Success(listOf(favourite("1", category = ProductCategory.CHEESE)))
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        viewModel.onAction(FavouritesAction.OnQueryChanged("name"))

        viewModel.onAction(FavouritesAction.OnGroupClicked(ProductCategory.CHEESE))

        assertTrue(groups(viewModel).single().expanded)
        assertNull(handle.get<ArrayList<String>>(KEY_COLLAPSED))

        viewModel.onAction(FavouritesAction.OnQueryChanged(""))

        assertTrue(groups(viewModel).single().canToggle)
        assertTrue(groups(viewModel).single().expanded)
    }

    private fun groups(viewModel: FavouritesViewModel): List<FavouriteGroupState> =
        assertIs<FavouritesContentState.ShowGroups>(viewModel.state.value.content).groups

    private fun items(viewModel: FavouritesViewModel): List<FavouriteItemState> = groups(viewModel).flatMap { it.items }

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

        override fun observeFavourites(nameQuery: String): Flow<List<FavouriteProduct>> = flow {
            queries += nameQuery
            if (nameQuery in pendingQueries) awaitCancellation()
            emit(result.getOrThrow())
        }

        override suspend fun addToFavourites(product: Product): Outcome<Unit> = Outcome.Success(Unit)

        override suspend fun isFavourite(barcode: Barcode): Outcome<Boolean> = Outcome.Success(false)

        override suspend fun removeFromFavourites(product: Product): Outcome<Unit> = Outcome.Success(Unit)
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
        const val KEY_COLLAPSED = "collapsed"

        fun favourite(
            barcode: String,
            dietRate: DietRate? = null,
            category: ProductCategory = ProductCategory.OTHER,
        ) =
            FavouriteProduct(
                barcode,
                "Name $barcode",
                "Brand",
                Instant.fromEpochMilliseconds(1_700_000_000_000),
                null,
                dietRate,
                null,
                category,
            )
    }
}
