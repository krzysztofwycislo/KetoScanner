package pl.morning.tower.karbo.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.ErrorReporter
import pl.morning.tower.karbo.core.common.EventQueue
import pl.morning.tower.karbo.core.common.fold
import pl.morning.tower.karbo.core.designsystem.event.AppEvent
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.feature.preferences.api.OnboardingRepository
import pl.morning.tower.karbo.navigation.AppStart
import pl.morning.tower.karbo.navigation.BottomNavSection
import pl.morning.tower.karbo.ui.AppAction.OnDestinationChanged
import pl.morning.tower.karbo.ui.AppAction.OnTabClicked
import pl.morning.tower.karbo.ui.AppNavigation.NavigateToTab
import pl.morning.tower.karbo.ui.AppNavigation.PopToTabRoot

internal const val KEY_SELECTED_TAB = "selectedTab"

internal class AppViewModel(
    appEvents: AppEvents,
    private val savedStateHandle: SavedStateHandle,
    private val onboarding: OnboardingRepository,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    val events: Flow<AppEvent> = appEvents.events

    private val navigationQueue = EventQueue<AppNavigation>(viewModelScope)
    val navigationEvents: Flow<AppNavigation> = navigationQueue.events

    val selectedTab: StateFlow<BottomNavSection> =
        savedStateHandle.getStateFlow(KEY_SELECTED_TAB, BottomNavSection.HOME)

    private val appStart = MutableStateFlow<AppStart?>(null)
    val start: StateFlow<AppStart?> = appStart.asStateFlow()

    fun onAction(action: AppAction) {
        when (action) {
            is OnDestinationChanged -> action.tab?.let(::select)
            is OnTabClicked -> onTabClicked(action.tab)
        }
    }

    private fun loadStart() {
        viewModelScope.launch {
            val isSetupDone = onboarding.isSetupDone().fold(
                onSuccess = { it },
                onFailure = { error ->
                    onStartFailed(error)
                    true
                },
            )
            appStart.value = if (isSetupDone) {
                AppStart.HOME
            } else {
                AppStart.ONBOARDING
            }
        }
    }

    private fun onStartFailed(error: AppError) {
        errorReporter.report(TAG, "Reading the setup state failed", error)
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

    init {
        loadStart()
    }

    private companion object {
        const val TAG = "AppViewModel"
    }
}
