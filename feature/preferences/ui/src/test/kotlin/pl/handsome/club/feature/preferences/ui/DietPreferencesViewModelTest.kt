package pl.handsome.club.feature.preferences.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation.NavigateBack
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStateFactory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pl.handsome.club.core.designsystem.R as DesignR

class DietPreferencesViewModelTest {

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
    fun `when created before any value then content is initial load`() = runTest {
        val viewModel = createViewModel()

        assertEquals(DietPreferencesContentState.InitialLoad, viewModel.state.value.content)
    }

    @Test
    fun `when the first value arrives then input is prefilled with it`() = runTest {
        val viewModel = createViewModel()

        preferences.emit(40)

        assertEquals("40", form(viewModel).input)
    }

    @Test
    fun `when an input was restored then it wins over the stored value`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_INPUT to "25")))

        preferences.emit(40)

        assertEquals("25", form(viewModel).input)
    }

    @Test
    fun `when a later value arrives then summary changes and input is kept`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(40)

        preferences.emit(55)

        val form = form(viewModel)
        assertEquals("40", form.input)
        assertEquals(DietPreferencesResources().summary(55), form.summary)
    }

    @Test
    fun `when input changes then non digits are dropped it is capped at three characters and saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        preferences.emit(40)

        viewModel.onAction(DietPreferencesAction.OnInputChanged("1a2-3 4"))

        assertEquals("123", form(viewModel).input)
        assertEquals("123", handle.get<String>(KEY_INPUT))
    }

    @Test
    fun `when input is empty zero or above the range then save marks it invalid and does not save`() = runTest {
        listOf("", "0", "1000").forEach { input ->
            val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_INPUT to input)))
            preferences.emit(40)

            viewModel.onAction(DietPreferencesAction.OnSaveClicked)

            assertNotNull(form(viewModel).inputError, "input \"$input\"")
        }
        assertTrue(preferences.saved.isEmpty())
        assertTrue(appEventsReceived.isEmpty())
    }

    @Test
    fun `when input is valid then it is saved keyboard is hidden and saved message is shown`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnInputChanged("0"))
        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        viewModel.onAction(DietPreferencesAction.OnInputChanged("55"))
        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(listOf(55), preferences.saved)
        assertNull(form(viewModel).inputError)
        assertEquals(
            listOf(AppEvent.HideKeyboard, AppEvent.ShowMessage(UiText.Res(R.string.preferences_saved))),
            appEventsReceived,
        )
    }

    @Test
    fun `when saving fails then failure is reported and failure message is shown`() = runTest {
        preferences.saveResult = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()
        preferences.emit(40)

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(listOf(AppError.Storage), errorReporter.reports.map { it.error })
        assertEquals(
            listOf(AppEvent.HideKeyboard, AppEvent.ShowMessage(UiText.Res(DesignR.string.designsystem_error_storage))),
            appEventsReceived,
        )
    }

    @Test
    fun `when reading fails then error is shown and reported and retry recovers`() = runTest {
        preferences.readFailure = AppError.Storage
        val viewModel = createViewModel()

        assertIs<DietPreferencesContentState.Error>(viewModel.state.value.content)
        assertEquals(1, errorReporter.reports.size)

        preferences.readFailure = null
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnRetryClicked)

        assertEquals("40", form(viewModel).input)
        assertEquals(2, preferences.subscriptions)
    }

    @Test
    fun `when back is clicked then keyboard is hidden and navigate back is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(DietPreferencesAction.OnBackClicked)

        assertEquals(listOf<AppEvent>(AppEvent.HideKeyboard), appEventsReceived)
        assertEquals(NavigateBack, viewModel.events.first())
    }

    private fun TestScope.createViewModel(handle: SavedStateHandle = SavedStateHandle()): DietPreferencesViewModel {
        val appEvents = AppEvents(backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { appEvents.events.toList(appEventsReceived) }
        val resources = DietPreferencesResources()
        return DietPreferencesViewModel(
            savedStateHandle = handle,
            preferences = preferences,
            appEvents = appEvents,
            stateFactory = DietPreferencesStateFactory(resources),
            resources = resources,
            errorReporter = errorReporter,
            applicationScope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )
    }

    private fun form(viewModel: DietPreferencesViewModel): DietPreferencesContentState.Form =
        assertIs<DietPreferencesContentState.Form>(viewModel.state.value.content)

    private class FakePreferences : DietPreferencesRepository {

        private val values = MutableSharedFlow<Outcome<DietPreferences>>(replay = 1)
        var readFailure: AppError? = null
        var saveResult: Outcome<Unit> = Outcome.Success(Unit)
        var subscriptions = 0
        val saved = mutableListOf<Int>()

        fun emit(amount: Int) {
            values.tryEmit(Outcome.Success(DietPreferences(amount)))
        }

        override fun observeDietPreferences(): Flow<Outcome<DietPreferences>> = flow {
            subscriptions++
            val failure = readFailure
            if (failure != null) emit(Outcome.Failure(failure))
            else values.collect { emit(it) }
        }

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = throw NotImplementedError()

        override suspend fun setMaxDailyCarbsAmount(amount: Int): Outcome<Unit> {
            saved += amount
            return saveResult
        }
    }

    private companion object {
        const val KEY_INPUT = "input"
    }
}
