package pl.morning.tower.karbo.feature.onboarding.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.designsystem.component.MessageTone
import pl.morning.tower.karbo.core.designsystem.event.AppEvent
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.testing.FakeErrorReporter
import pl.morning.tower.karbo.feature.onboarding.ui.event.OnboardingAction
import pl.morning.tower.karbo.feature.onboarding.ui.event.OnboardingEvent
import pl.morning.tower.karbo.feature.onboarding.ui.event.OnboardingEvent.OnboardingNavigation.NavigateToScan
import pl.morning.tower.karbo.feature.onboarding.ui.state.OnboardingStateFactory
import pl.morning.tower.karbo.feature.preferences.api.OnboardingRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pl.morning.tower.karbo.core.designsystem.R as DesignR

class OnboardingViewModelTest {

    private val onboarding = FakeOnboarding()
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
    fun `when created then keto is selected`() = runTest {
        val viewModel = createViewModel()

        assertEquals(DietPreset.KETO, selectedPreset(viewModel))
    }

    @Test
    fun `when a preset is selected then state and saved state are updated and nothing is saved`() = runTest {
        val handle = SavedStateHandle()
        val viewModel = createViewModel(handle)

        viewModel.onAction(OnboardingAction.OnPresetSelected(DietPreset.LOW_CARB))

        assertEquals(DietPreset.LOW_CARB, selectedPreset(viewModel))
        assertEquals("LOW_CARB", handle.get<String>(KEY_PRESET))
        assertTrue(onboarding.completed.isEmpty())
    }

    @Test
    fun `when a preset name was saved then it is restored`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_PRESET to "STRICT_KETO")))

        assertEquals(DietPreset.STRICT_KETO, selectedPreset(viewModel))
    }

    @Test
    fun `when an unknown preset name was saved then keto is selected`() = runTest {
        val viewModel = createViewModel(SavedStateHandle(mapOf(KEY_PRESET to "PALEO")))

        assertEquals(DietPreset.KETO, selectedPreset(viewModel))
    }

    @Test
    fun `when start scanning is clicked then setup completes with the selected preset and scan opens`() = runTest {
        val viewModel = createViewModel()
        val screenEvents = collectScreenEvents(viewModel)
        viewModel.onAction(OnboardingAction.OnPresetSelected(DietPreset.LOW_CARB))

        viewModel.onAction(OnboardingAction.OnStartScanningClicked)

        assertEquals(listOf(DietPreset.LOW_CARB), onboarding.completed)
        assertEquals(listOf<OnboardingEvent>(NavigateToScan), screenEvents)
        assertTrue(appEventsReceived.isEmpty())
    }

    @Test
    fun `when skip is clicked then setup completes with keto whatever is selected and scan opens`() = runTest {
        val viewModel = createViewModel()
        val screenEvents = collectScreenEvents(viewModel)
        viewModel.onAction(OnboardingAction.OnPresetSelected(DietPreset.STRICT_KETO))

        viewModel.onAction(OnboardingAction.OnSkipClicked)

        assertEquals(listOf(DietPreset.KETO), onboarding.completed)
        assertEquals(listOf<OnboardingEvent>(NavigateToScan), screenEvents)
    }

    @Test
    fun `when completing setup fails then the error is reported and shown and scan does not open`() = runTest {
        onboarding.result = Outcome.Failure(AppError.Storage)
        val viewModel = createViewModel()
        val screenEvents = collectScreenEvents(viewModel)

        viewModel.onAction(OnboardingAction.OnStartScanningClicked)

        assertEquals(listOf(AppError.Storage), errorReporter.reports.map { it.error })
        assertEquals(
            listOf<AppEvent>(
                AppEvent.ShowMessage(UiText.Res(DesignR.string.designsystem_error_storage), MessageTone.Error),
            ),
            appEventsReceived,
        )
        assertTrue(screenEvents.isEmpty())
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `when clicked again while saving then it is ignored and saving is shown until it finishes`() = runTest {
        val gate = CompletableDeferred<Unit>()
        onboarding.gate = gate
        val viewModel = createViewModel()
        val screenEvents = collectScreenEvents(viewModel)

        viewModel.onAction(OnboardingAction.OnStartScanningClicked)
        viewModel.onAction(OnboardingAction.OnSkipClicked)
        viewModel.onAction(OnboardingAction.OnStartScanningClicked)

        assertEquals(listOf(DietPreset.KETO), onboarding.completed)
        assertTrue(viewModel.state.value.isSaving)

        gate.complete(Unit)
        testScheduler.runCurrent()

        assertFalse(viewModel.state.value.isSaving)
        assertEquals(listOf<OnboardingEvent>(NavigateToScan), screenEvents)
    }

    private fun TestScope.createViewModel(handle: SavedStateHandle = SavedStateHandle()): OnboardingViewModel {
        val appEvents = AppEvents(backgroundScope)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { appEvents.events.toList(appEventsReceived) }
        val resources = OnboardingResources()
        return OnboardingViewModel(
            savedStateHandle = handle,
            onboarding = onboarding,
            appEvents = appEvents,
            stateFactory = OnboardingStateFactory(resources),
            resources = resources,
            errorReporter = errorReporter,
        )
    }

    private fun TestScope.collectScreenEvents(viewModel: OnboardingViewModel): List<OnboardingEvent> {
        val received = mutableListOf<OnboardingEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.toList(received) }
        return received
    }

    private fun selectedPreset(viewModel: OnboardingViewModel): DietPreset =
        viewModel.state.value.options.single { it.isSelected }.preset

    private class FakeOnboarding : OnboardingRepository {

        var result: Outcome<Unit> = Outcome.Success(Unit)
        var gate: CompletableDeferred<Unit>? = null
        val completed = mutableListOf<DietPreset>()

        override suspend fun isSetupDone(): Outcome<Boolean> = throw NotImplementedError()

        override suspend fun completeSetup(preset: DietPreset): Outcome<Unit> {
            completed += preset
            gate?.await()
            return result
        }
    }

    private companion object {
        const val KEY_PRESET = "preset"
    }
}
