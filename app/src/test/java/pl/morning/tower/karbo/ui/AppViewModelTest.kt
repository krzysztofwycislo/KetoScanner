package pl.handsome.club.ketoscanner.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.ketoscanner.navigation.BottomNavSection
import pl.handsome.club.ketoscanner.ui.AppAction.OnDestinationChanged
import pl.handsome.club.ketoscanner.ui.AppAction.OnTabClicked
import pl.handsome.club.ketoscanner.ui.AppNavigation.NavigateToTab
import pl.handsome.club.ketoscanner.ui.AppNavigation.PopToTabRoot
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppViewModelTest {

    private val savedStateHandle = SavedStateHandle()

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

    private fun createViewModel() = AppViewModel(AppEvents(TestScope()), savedStateHandle)
}
