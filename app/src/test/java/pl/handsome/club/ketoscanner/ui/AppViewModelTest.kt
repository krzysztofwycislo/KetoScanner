package pl.handsome.club.ketoscanner.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.test.TestScope
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.ketoscanner.navigation.TopLevelTab
import pl.handsome.club.ketoscanner.ui.AppAction.OnDestinationChanged
import pl.handsome.club.ketoscanner.ui.AppAction.OnTabClicked
import kotlin.test.Test
import kotlin.test.assertEquals

class AppViewModelTest {

    private val savedStateHandle = SavedStateHandle()

    @Test
    fun `when nothing is saved then home is selected`() {
        val viewModel = createViewModel()

        assertEquals(TopLevelTab.HOME, viewModel.selectedTab.value)
    }

    @Test
    fun `when the destination is a top-level tab then it is selected`() {
        val viewModel = createViewModel()

        viewModel.onAction(OnDestinationChanged(TopLevelTab.SCAN))

        assertEquals(TopLevelTab.SCAN, viewModel.selectedTab.value)
    }

    @Test
    fun `when the destination has no tab then the last tab stays selected`() {
        val viewModel = createViewModel()
        viewModel.onAction(OnDestinationChanged(TopLevelTab.FAVOURITES))

        viewModel.onAction(OnDestinationChanged(null))

        assertEquals(TopLevelTab.FAVOURITES, viewModel.selectedTab.value)
    }

    @Test
    fun `when a tab is clicked then it is selected`() {
        val viewModel = createViewModel()

        viewModel.onAction(OnTabClicked(TopLevelTab.FAVOURITES))

        assertEquals(TopLevelTab.FAVOURITES, viewModel.selectedTab.value)
    }

    @Test
    fun `when the handle has a saved tab then it is restored`() {
        savedStateHandle[KEY_SELECTED_TAB] = TopLevelTab.SCAN

        val viewModel = createViewModel()

        assertEquals(TopLevelTab.SCAN, viewModel.selectedTab.value)
    }

    @Test
    fun `when a tab is selected then it is written to the handle`() {
        val viewModel = createViewModel()

        viewModel.onAction(OnTabClicked(TopLevelTab.SCAN))

        assertEquals(TopLevelTab.SCAN, savedStateHandle.get<TopLevelTab>(KEY_SELECTED_TAB))
    }

    private fun createViewModel() = AppViewModel(AppEvents(TestScope()), savedStateHandle)
}
