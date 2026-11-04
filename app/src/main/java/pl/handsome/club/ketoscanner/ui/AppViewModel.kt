package pl.handsome.club.ketoscanner.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import pl.handsome.club.core.common.EventQueue
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.ketoscanner.navigation.BottomNavSection
import pl.handsome.club.ketoscanner.ui.AppAction.OnDestinationChanged
import pl.handsome.club.ketoscanner.ui.AppAction.OnTabClicked
import pl.handsome.club.ketoscanner.ui.AppNavigation.NavigateToTab
import pl.handsome.club.ketoscanner.ui.AppNavigation.PopToTabRoot

internal const val KEY_SELECTED_TAB = "selectedTab"

internal class AppViewModel(
    appEvents: AppEvents,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val events: Flow<AppEvent> = appEvents.events

    private val navigationQueue = EventQueue<AppNavigation>(viewModelScope)
    val navigationEvents: Flow<AppNavigation> = navigationQueue.events

    val selectedTab: StateFlow<BottomNavSection> =
        savedStateHandle.getStateFlow(KEY_SELECTED_TAB, BottomNavSection.HOME)

    fun onAction(action: AppAction) {
        when (action) {
            is OnDestinationChanged -> action.tab?.let(::select)
            is OnTabClicked -> onTabClicked(action.tab)
        }
    }

    private fun onTabClicked(tab: BottomNavSection) {
        val navigation = if (tab == selectedTab.value) {
            PopToTabRoot(tab)
        } else {
            NavigateToTab(tab)
        }
        select(tab)
        navigationQueue send navigation
    }

    private fun select(tab: BottomNavSection) {
        savedStateHandle[KEY_SELECTED_TAB] = tab
    }
}
