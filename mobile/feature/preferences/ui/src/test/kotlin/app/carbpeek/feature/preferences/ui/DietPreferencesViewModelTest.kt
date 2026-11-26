package app.carbpeek.feature.preferences.ui

import androidx.lifecycle.SavedStateHandle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.AppErrorException
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.designsystem.component.MessageTone
import app.carbpeek.core.designsystem.event.AppEvent
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.testing.FakeErrorReporter
import app.carbpeek.feature.preferences.api.ProfileSettingsRepository
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.preferences.api.model.ProfileSettings
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction
import app.carbpeek.feature.preferences.ui.event.DietPreferencesEvent
import app.carbpeek.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation.NavigateBack
import app.carbpeek.feature.preferences.ui.state.DietPreferencesContentState
import app.carbpeek.feature.preferences.ui.state.DietPreferencesStateFactory
import app.carbpeek.feature.preferences.ui.state.DietProfileOption
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import app.carbpeek.core.designsystem.R as DesignR

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
    }

    @Test
    fun `when the first value matches no preset then keto is selected`() = runTest {
        val viewModel = createViewModel()

        preferences.emit(amount = 35, mode = DietMode.LOW_CARB)

        assertEquals(DietProfileOption.KETO, selectedOption(viewModel))
    }

    @Test
    fun `when an option was restored then it wins over the stored value`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_OPTION to "LOW_CARB")))

        preferences.emit(40)

        assertEquals(DietProfileOption.LOW_CARB, selectedOption(viewModel))
    }

    @Test
    fun `when the restored option does not exist then the stored profile is selected`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_OPTION to "UNKNOWN")))

        preferences.emit(20)

        assertEquals(DietProfileOption.STRICT_KETO, selectedOption(viewModel))
    }

    @Test
    fun `when no diet is stored then its option is selected`() = runTest {
        val viewModel = createViewModel()

        preferences.emit(DietPreferences.NoDiet)

        assertEquals(DietProfileOption.NO_DIET, selectedOption(viewModel))
    }

    @Test
    fun `when the no diet option was restored then it wins over the stored value`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_OPTION to "NO_DIET")))

        preferences.emit(40)

        assertEquals(DietProfileOption.NO_DIET, selectedOption(viewModel))
    }

    @Test
    fun `when save is clicked with no diet then no diet is saved and the screen closes with the same message`() =
        runTest {
            val viewModel = createViewModel()
            val screenEvents = collectScreenEvents(viewModel)
            preferences.emit(40)
            viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.NO_DIET))

            viewModel.onAction(DietPreferencesAction.OnSaveClicked)

            assertEquals(listOf(DietPreferences.NoDiet), preferences.saved)
            assertEquals(
                listOf<AppEvent>(
                    AppEvent.ShowMessage(UiText.Res(R.string.preferences_saved), MessageTone.Success),
                ),
                appEventsReceived,
            )
            assertEquals(listOf<DietPreferencesEvent>(NavigateBack), screenEvents)
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
    fun `when save is clicked with strict keto then it is saved and the screen closes with a message`() = runTest {
        val viewModel = createViewModel()
        val screenEvents = collectScreenEvents(viewModel)
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.STRICT_KETO))

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(listOf(DietPreferences(20, DietMode.KETO)), preferences.saved)
        assertEquals(
            listOf<AppEvent>(
                AppEvent.ShowMessage(UiText.Res(R.string.preferences_saved), MessageTone.Success),
            ),
            appEventsReceived,
        )
        assertEquals(listOf<DietPreferencesEvent>(NavigateBack), screenEvents)
    }

    @Test
    fun `when save is clicked with low carb then that profile is saved`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.LOW_CARB))

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(
            listOf(DietPreferences(maxDailyCarbsAmount = 100, dietMode = DietMode.LOW_CARB)),
            preferences.saved,
        )
    }

    @Test
    fun `when the stored profile matches no preset then keto is saved only after save is clicked`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(amount = 80, mode = DietMode.LOW_CARB)
        assertTrue(preferences.saved.isEmpty())

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(
            listOf(DietPreferences(maxDailyCarbsAmount = 40, dietMode = DietMode.KETO)),
            preferences.saved,
        )
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
            listOf<AppEvent>(
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
            listOf<AppEvent>(
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
    fun `when nothing else is stored then the details switch is off`() = runTest {
        val viewModel = createViewModel()

        preferences.emit(40)

        assertEquals(false, isDetailsSwitchOn(viewModel))
    }

    @Test
    fun `when the details setting is stored on then the switch is on`() = runTest {
        val viewModel = createViewModel()

        preferences.emit(preferences = DietPreset.LOW_CARB.preferences, opensDetailsRightAway = true)

        assertEquals(true, isDetailsSwitchOn(viewModel))
    }

    @Test
    fun `when the details switch is changed then it is written to the saved state and nothing is saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)
        preferences.emit(40)

        viewModel.onAction(DietPreferencesAction.OnDetailsSwitchChanged(isOn = true))

        assertEquals(true, isDetailsSwitchOn(viewModel))
        assertEquals(true, handle.get<Boolean>(KEY_OPENS_DETAILS_RIGHT_AWAY))
        assertTrue(preferences.savedSettings.isEmpty())
        assertTrue(appEventsReceived.isEmpty())

        viewModel.onAction(DietPreferencesAction.OnDetailsSwitchChanged(isOn = false))

        assertEquals(false, isDetailsSwitchOn(viewModel))
        assertEquals(false, handle.get<Boolean>(KEY_OPENS_DETAILS_RIGHT_AWAY))
    }

    @Test
    fun `when the details choice was restored then it wins over the stored and a later value`() = runTest {
        listOf(true, false).forEach { restored ->
            val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_OPENS_DETAILS_RIGHT_AWAY to restored)))

            preferences.emit(preferences = DietPreset.KETO.preferences, opensDetailsRightAway = !restored)
            assertEquals(restored, isDetailsSwitchOn(viewModel))

            preferences.emit(preferences = DietPreset.LOW_CARB.preferences, opensDetailsRightAway = !restored)
            assertEquals(restored, isDetailsSwitchOn(viewModel))
        }
    }

    @Test
    fun `when a later value arrives then the details choice is kept`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(40)

        preferences.emit(preferences = DietPreset.KETO.preferences, opensDetailsRightAway = true)

        assertEquals(false, isDetailsSwitchOn(viewModel))
    }

    @Test
    fun `when save is clicked then the profile and the details choice are saved together`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.LOW_CARB))
        viewModel.onAction(DietPreferencesAction.OnDetailsSwitchChanged(isOn = true))

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(
            listOf(
                ProfileSettings(dietPreferences = DietPreset.LOW_CARB.preferences, opensDetailsRightAway = true),
            ),
            preferences.savedSettings,
        )
    }

    @Test
    fun `when the switch is on and no diet is picked and left again then the switch is still on`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnDetailsSwitchChanged(isOn = true))

        viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.NO_DIET))
        assertNull(isDetailsSwitchOn(viewModel))
        viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.KETO))

        assertEquals(true, isDetailsSwitchOn(viewModel))
    }

    @Test
    fun `when no diet is saved then the details choice is saved unchanged`() = runTest {
        val viewModel = createViewModel()
        preferences.emit(preferences = DietPreset.KETO.preferences, opensDetailsRightAway = true)
        viewModel.onAction(DietPreferencesAction.OnOptionSelected(DietProfileOption.NO_DIET))

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(
            listOf(ProfileSettings(dietPreferences = DietPreferences.NoDiet, opensDetailsRightAway = true)),
            preferences.savedSettings,
        )
    }

    @Test
    fun `when saving fails then the details choice and the screen stay`() = runTest {
        preferences.saveResult = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()
        val screenEvents = collectScreenEvents(viewModel)
        preferences.emit(40)
        viewModel.onAction(DietPreferencesAction.OnDetailsSwitchChanged(isOn = true))

        viewModel.onAction(DietPreferencesAction.OnSaveClicked)

        assertEquals(true, isDetailsSwitchOn(viewModel))
        assertTrue(screenEvents.isEmpty())
    }

    @Test
    fun `when back is clicked then navigate back is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(DietPreferencesAction.OnBackClicked)

        assertTrue(appEventsReceived.isEmpty())
        assertEquals(NavigateBack, viewModel.events.first())
    }

    private fun TestScope.createViewModel(handle: SavedStateHandle = SavedStateHandle()): DietPreferencesViewModel {
        val appEvents = AppEvents(backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { appEvents.events.toList(appEventsReceived) }
        val resources = DietPreferencesResources()
        return DietPreferencesViewModel(
            savedStateHandle = handle,
            profileSettings = preferences,
            appEvents = appEvents,
            stateFactory = DietPreferencesStateFactory(resources),
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

    private fun isDetailsSwitchOn(viewModel: DietPreferencesViewModel): Boolean? =
        form(viewModel).detailsSwitch?.isOn

    private class FakePreferences : ProfileSettingsRepository {

        private val values = MutableSharedFlow<ProfileSettings>(replay = 1)
        var readFailure: AppError? = null
        var saveResult: Outcome<Unit> = Outcome.Success(Unit)
        var saveGate: CompletableDeferred<Unit>? = null
        var subscriptions = 0
        val savedSettings = mutableListOf<ProfileSettings>()
        val saved: List<DietPreferences>
            get() = savedSettings.map { it.dietPreferences }

        fun emit(amount: Int, mode: DietMode = DietMode.KETO) {
            emit(DietPreferences(amount, mode))
        }

        fun emit(preferences: DietPreferences, opensDetailsRightAway: Boolean = false) {
            values.tryEmit(
                ProfileSettings(dietPreferences = preferences, opensDetailsRightAway = opensDetailsRightAway),
            )
        }

        override fun observeSettings(): Flow<ProfileSettings> = flow {
            subscriptions++
            val failure = readFailure
            if (failure != null) {
                throw AppErrorException(failure)
            }
            values.collect { emit(it) }
        }

        override suspend fun saveSettings(settings: ProfileSettings): Outcome<Unit> {
            savedSettings += settings
            saveGate?.await()
            return saveResult
        }
    }

    private companion object {
        const val KEY_OPTION = "option"
        const val KEY_OPENS_DETAILS_RIGHT_AWAY = "opensDetailsRightAway"
    }
}
