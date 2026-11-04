package pl.handsome.club.feature.preferences.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
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
import pl.handsome.club.core.designsystem.component.MessageTone
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.testing.FakeErrorReporter
import pl.handsome.club.feature.analysis.api.DietRatingLimits
import pl.handsome.club.feature.analysis.api.model.CarbLimits
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesEvent
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation.NavigateBack
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesContentState
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStateFactory
import pl.handsome.club.feature.preferences.ui.state.DietProfileOption
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    fun `when the first value matches a preset then that option is selected`() = runTest {
        val viewModel = createViewModel()

        preferences.emit(20)

        assertEquals(DietProfileOption.STRICT_KETO, selectedOption(viewModel))
        assertNull(form(viewModel).custom)
    }

    @Test
    fun `when the first value matches no preset then custom is selected with input and mode prefilled`() =
        runTest {
            val viewModel = createViewModel()

            preferences.emit(35, DietMode.LOW_CARB)

            assertEquals(DietProfileOption.CUSTOM, selectedOption(viewModel))
            val custom = assertNotNull(form(viewModel).custom)
            assertEquals("35", custom.input)
            assertEquals(DietMode.LOW_CARB, custom.modes.single { it.isSelected }.mode)
        }

    @Test
    fun `when option input and custom mode were restored then they win over the stored value`() = runTest {
        val handle = SavedStateHandle(
            mapOf(KEY_OPTION to "CUSTOM", KEY_INPUT to "25", KEY_CUSTOM_MODE to "LOW_CARB"),
        )
        val viewModel = createViewModel(handle)

        preferences.emit(40)

        assertEquals(DietProfileOption.CUSTOM, selectedOption(viewModel))
        val custom = assertNotNull(form(viewModel).custom)
        assertEquals("25", custom.input)
        assertEquals(DietMode.LOW_CARB, custom.modes.single { it.isSelected }.mode)
    }

    @Test
    fun `when a later value arrives then the form selection is kept`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(40)

        preferences.emit(100, DietMode.LOW_CARB)

        assertEquals(DietProfileOption.KETO, selectedOption(viewModel))
    }

    @Test
    fun `when an option is selected then it is written to the saved state and nothing is saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        preferences.emit(40)

        viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.LOW_CARB))

        assertEquals(DietProfileOption.LOW_CARB, selectedOption(viewModel))
        assertEquals("LOW_CARB", handle.get<String>(KEY_OPTION))
        assertTrue(preferences.saved.isEmpty())
        assertTrue(appEventsReceived.isEmpty())
    }

    @Test
    fun `when a custom mode is selected then it is written to the saved state and nothing is saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        preferences.emit(35)

        viewModel.onAction(DietPreferencesAction.OnCustomModeSelected(DietMode.LOW_CARB))

        assertEquals(DietMode.LOW_CARB, assertNotNull(form(viewModel).custom).modes.single { it.isSelected }.mode)
        assertEquals("LOW_CARB", handle.get<String>(KEY_CUSTOM_MODE))
        assertTrue(preferences.saved.isEmpty())
        assertTrue(appEventsReceived.isEmpty())
    }

    @Test
    fun `when input changes then non digits are dropped it is capped at three characters and saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        preferences.emit(35)

        viewModel.onAction(DietPreferencesAction.OnInputChanged("1a2-3 4"))

        assertEquals("123", assertNotNull(form(viewModel).custom).input)
        assertEquals("123", handle.get<String>(KEY_INPUT))
        assertTrue(preferences.saved.isEmpty())
    }

    @Test
    fun `when save is clicked with strict keto then it is saved and the screen closes with a message`() = runTest {
        val viewModel = createViewModel()
        val screenEvents = collectScreenEvents(viewModel)
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.STRICT_KETO))

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(listOf(DietPreferences(20, DietMode.KETO)), preferences.saved)
        assertEquals(
            listOf(
                AppEvent.HideKeyboard,
                AppEvent.ShowMessage(UiText.Res(R.string.preferences_saved), MessageTone.Success),
            ),
            appEventsReceived,
        )
        assertEquals(listOf<DietPreferencesEvent>(NavigateBack), screenEvents)
    }

    @Test
    fun `when save is clicked with custom low carb 80 then that profile is saved`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.CUSTOM))
        viewModel.onAction(DietPreferencesAction.OnCustomModeSelected(DietMode.LOW_CARB))
        viewModel.onAction(DietPreferencesAction.OnInputChanged("80"))

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(listOf(DietPreferences(80, DietMode.LOW_CARB)), preferences.saved)
    }

    @Test
    fun `when custom input is empty zero or above the range then save marks it invalid and does not save`() =
        runTest {
            listOf("", "0", "1000").forEach { input ->
                val viewModel = createViewModel(
                    SavedStateHandle(mapOf(KEY_OPTION to "CUSTOM", KEY_INPUT to input)),
                )
                preferences.emit(40)

                viewModel.onAction(DietPreferencesAction.OnSaveClicked)

                assertNotNull(assertNotNull(form(viewModel).custom).inputError, "input \"$input\"")
            }
            assertTrue(preferences.saved.isEmpty())
            assertTrue(appEventsReceived.isEmpty())
        }

    @Test
    fun `when a valid custom value is saved after an invalid one then the error is cleared`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_OPTION to "CUSTOM")))
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnInputChanged("0"))
        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        viewModel.onAction(DietPreferencesAction.OnInputChanged("55"))
        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(listOf(DietPreferences(55, DietMode.KETO)), preferences.saved)
        assertNull(assertNotNull(form(viewModel).custom).inputError)
    }

    @Test
    fun `when save is tapped again while saving then it is ignored and saving is shown until it finishes`() = runTest {
        val gate = CompletableDeferred<Unit>()
        preferences.saveGate = gate
        val viewModel = createViewModel()
        preferences.emit(40)

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)
        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(listOf(DietPreferences(40, DietMode.KETO)), preferences.saved)
        assertFalse(form(viewModel).isSaveEnabled)

        gate.complete(Unit)
        testScheduler.runCurrent()

        assertTrue(form(viewModel).isSaveEnabled)
        assertEquals(
            listOf(
                AppEvent.HideKeyboard,
                AppEvent.ShowMessage(UiText.Res(R.string.preferences_saved), MessageTone.Success),
            ),
            appEventsReceived,
        )
    }

    @Test
    fun `when saving fails then failure is reported the message is shown and the screen stays`() = runTest {
        preferences.saveResult = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()
        val screenEvents = collectScreenEvents(viewModel)
        preferences.emit(40)

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(listOf(AppError.Storage), errorReporter.reports.map { it.error })
        assertEquals(
            listOf(
                AppEvent.HideKeyboard,
                AppEvent.ShowMessage(UiText.Res(DesignR.string.designsystem_error_storage), MessageTone.Error),
            ),
            appEventsReceived,
        )
        assertTrue(screenEvents.isEmpty())
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

        assertEquals(DietProfileOption.KETO, selectedOption(viewModel))
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
            stateFactory = DietPreferencesStateFactory(resources, FakeRatingLimits()),
            resources = resources,
            errorReporter = errorReporter,
            applicationScope = CoroutineScope(
                backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler),
            ),
        )
    }

    private fun TestScope.collectScreenEvents(viewModel: DietPreferencesViewModel): List<DietPreferencesEvent> {
        val received = mutableListOf<DietPreferencesEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.toList(received) }
        return received
    }

    private fun form(viewModel: DietPreferencesViewModel): DietPreferencesContentState.Form =
        assertIs<DietPreferencesContentState.Form>(viewModel.state.value.content)

    private fun selectedOption(viewModel: DietPreferencesViewModel): DietProfileOption =
        form(viewModel).options.single { it.isSelected }.option

    private class FakeRatingLimits : DietRatingLimits {
        override fun carbsPer100g(preferences: DietPreferences): CarbLimits =
            CarbLimits(preferences.maxDailyCarbsAmount / 3.0, preferences.maxDailyCarbsAmount / 2.0)
    }

    private class FakePreferences : DietPreferencesRepository {

        private val values = MutableSharedFlow<Outcome<DietPreferences>>(replay = 1)
        var readFailure: AppError? = null
        var saveResult: Outcome<Unit> = Outcome.Success(Unit)
        var saveGate: CompletableDeferred<Unit>? = null
        var subscriptions = 0
        val saved = mutableListOf<DietPreferences>()

        fun emit(amount: Int, mode: DietMode = DietMode.KETO) {
            values.tryEmit(Outcome.Success(DietPreferences(amount, mode)))
        }

        override fun observeDietPreferences(): Flow<Outcome<DietPreferences>> = flow {
            subscriptions++
            val failure = readFailure
            if (failure != null) emit(Outcome.Failure(failure))
            else values.collect { emit(it) }
        }

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = throw NotImplementedError()

        override suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit> {
            saved += preferences
            saveGate?.await()
            return saveResult
        }
    }

    private companion object {
        const val KEY_OPTION = "option"
        const val KEY_INPUT = "input"
        const val KEY_CUSTOM_MODE = "customMode"
    }
}
