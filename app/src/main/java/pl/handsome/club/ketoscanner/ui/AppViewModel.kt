package pl.handsome.club.ketoscanner.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import pl.handsome.club.core.designsystem.event.AppEvent
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.ketoscanner.navigation.TopLevelTab
import pl.handsome.club.ketoscanner.ui.AppAction.OnDestinationChanged
import pl.handsome.club.ketoscanner.ui.AppAction.OnTabClicked

internal const val KEY_SELECTED_TAB = "selectedTab"

internal class AppViewModel(
    appEvents: AppEvents,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val events: Flow<AppEvent> = appEvents.events

    val selectedTab: StateFlow<TopLevelTab> = savedStateHandle.getStateFlow(KEY_SELECTED_TAB, TopLevelTab.HOME)

    fun onAction(action: AppAction) {
        when (action) {
            is OnDestinationChanged -> action.tab?.let(::select)
            is OnTabClicked -> select(action.tab)
        }
    }

    private fun select(tab: TopLevelTab) {
        savedStateHandle[KEY_SELECTED_TAB] = tab
    }
}
