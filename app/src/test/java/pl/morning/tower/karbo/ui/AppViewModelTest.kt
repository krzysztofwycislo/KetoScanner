package pl.morning.tower.karbo.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.core.testing.FakeErrorReporter
import pl.morning.tower.karbo.feature.preferences.api.OnboardingRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset
import pl.morning.tower.karbo.navigation.AppStart
import pl.morning.tower.karbo.navigation.BottomNavSection
import pl.morning.tower.karbo.ui.AppAction.OnDestinationChanged
import pl.morning.tower.karbo.ui.AppAction.OnTabClicked
import pl.morning.tower.karbo.ui.AppNavigation.NavigateToTab
import pl.morning.tower.karbo.ui.AppNavigation.PopToTabRoot
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppViewModelTest {

    private val savedStateHandle = SavedStateHandle()
    private val onboarding = FakeOnboarding()
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
    fun `when nothing is saved then home is selected`() {
        val viewModel = createViewModel()

        assertEquals(BottomNavSection.HOME, viewModel.selectedTab.value)
    }

    @Test
    fun `when the destination is a top-level tab then it is selected`() {
        val viewModel = createViewModel()

        viewModel.onAction(OnDestinationChanged(BottomNavSection.SCAN))

        assertEquals(BottomNavSection.SCAN, viewModel.selectedTab.value)
    }

    @Test
    fun `when the destination has no tab then the last tab stays selected`() {
        val viewModel = createViewModel()
        viewModel.onAction(OnDestinationChanged(BottomNavSection.FAVOURITES))

        viewModel.onAction(OnDestinationChanged(null))

        assertEquals(BottomNavSection.FAVOURITES, viewModel.selectedTab.value)
    }

    @Test
    fun `when a tab is clicked then it is selected`() {
        val viewModel = createViewModel()

        viewModel.onAction(OnTabClicked(BottomNavSection.FAVOURITES))

        assertEquals(BottomNavSection.FAVOURITES, viewModel.selectedTab.value)
    }

    @Test
    fun `when the handle has a saved tab then it is restored`() {
        savedStateHandle[KEY_SELECTED_TAB] = BottomNavSection.SCAN

        val viewModel = createViewModel()

        assertEquals(BottomNavSection.SCAN, viewModel.selectedTab.value)
    }

    @Test
    fun `when a tab is selected then it is written to the handle`() {
        val viewModel = createViewModel()

        viewModel.onAction(OnTabClicked(BottomNavSection.SCAN))

        assertEquals(BottomNavSection.SCAN, savedStateHandle.get<BottomNavSection>(KEY_SELECTED_TAB))
    }

    @Test
    fun `when another tab is clicked then navigate to that tab is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(OnTabClicked(BottomNavSection.SCAN))

        assertEquals(NavigateToTab(BottomNavSection.SCAN), viewModel.navigationEvents.first())
    }

    @Test
    fun `when the selected tab is clicked then pop to its root is emitted`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(OnTabClicked(BottomNavSection.HOME))

        assertEquals(PopToTabRoot(BottomNavSection.HOME), viewModel.navigationEvents.first())
    }

    @Test
    fun `when a tab is clicked twice then it navigates and then pops`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(OnTabClicked(BottomNavSection.FAVOURITES))
        viewModel.onAction(OnTabClicked(BottomNavSection.FAVOURITES))

        assertEquals(
            listOf(
                NavigateToTab(BottomNavSection.FAVOURITES),
                PopToTabRoot(BottomNavSection.FAVOURITES),
            ),
            viewModel.navigationEvents.take(2).toList(),
        )
    }

    @Test
    fun `when the tab selected by a destination change is clicked then pop to its root is emitted`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(OnDestinationChanged(BottomNavSection.SCAN))

        viewModel.onAction(OnTabClicked(BottomNavSection.SCAN))

        assertEquals(PopToTabRoot(BottomNavSection.SCAN), viewModel.navigationEvents.first())
    }

    @Test
    fun `when the destination has no tab then clicking the last tab pops to its root`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(OnDestinationChanged(BottomNavSection.FAVOURITES))
        viewModel.onAction(OnDestinationChanged(null))

        viewModel.onAction(OnTabClicked(BottomNavSection.FAVOURITES))

        assertEquals(PopToTabRoot(BottomNavSection.FAVOURITES), viewModel.navigationEvents.first())
    }

    @Test
    fun `when the saved tab is clicked after a restore then pop to its root is emitted`() = runTest {
        savedStateHandle[KEY_SELECTED_TAB] = BottomNavSection.SCAN
        val viewModel = createViewModel()

        viewModel.onAction(OnTabClicked(BottomNavSection.SCAN))

        assertEquals(PopToTabRoot(BottomNavSection.SCAN), viewModel.navigationEvents.first())
    }

    @Test
    fun `when setup is done then the app starts on home`() {
        onboarding.isSetupDone = { Outcome.Success(true) }

        val viewModel = createViewModel()

        assertEquals(AppStart.HOME, viewModel.start.value)
        assertEquals(emptyList(), errorReporter.reports)
    }

    @Test
    fun `when setup is not done then the app starts on onboarding`() {
        onboarding.isSetupDone = { Outcome.Success(false) }

        val viewModel = createViewModel()

        assertEquals(AppStart.ONBOARDING, viewModel.start.value)
    }

    @Test
    fun `when reading the setup state fails then the error is reported and the app starts on home`() {
        onboarding.isSetupDone = { Outcome.Failure(AppError.Storage) }

        val viewModel = createViewModel()

        assertEquals(AppStart.HOME, viewModel.start.value)
        assertEquals(listOf(AppError.Storage), errorReporter.reports.map { it.error })
    }

    @Test
    fun `when the setup state is not read yet then the start is unknown`() {
        onboarding.isSetupDone = { awaitCancellation() }

        val viewModel = createViewModel()

        assertNull(viewModel.start.value)
    }

    private fun createViewModel() = AppViewModel(
        appEvents = AppEvents(TestScope()),
        savedStateHandle = savedStateHandle,
        onboarding = onboarding,
        errorReporter = errorReporter,
    )

    private class FakeOnboarding : OnboardingRepository {

        var isSetupDone: suspend () -> Outcome<Boolean> = { Outcome.Success(true) }

        override suspend fun isSetupDone(): Outcome<Boolean> = isSetupDone.invoke()

        override suspend fun completeSetup(preset: DietPreset): Outcome<Unit> = throw NotImplementedError()
    }
}
