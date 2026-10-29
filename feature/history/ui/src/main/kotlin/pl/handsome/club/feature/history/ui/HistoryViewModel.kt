package pl.handsome.club.feature.history.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.AppLogger
import pl.handsome.club.core.common.EventQueue
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.common.onFailure
import pl.handsome.club.core.common.onSuccess
import pl.handsome.club.feature.history.api.AnalysisHistoryRepository
import pl.handsome.club.feature.history.ui.event.HistoryAction
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnAboutClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnDietPreferencesClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnEntryClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnMenuClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnMenuDismissed
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnRetryClicked
import pl.handsome.club.feature.history.ui.event.HistoryAction.OnStart
import pl.handsome.club.feature.history.ui.event.HistoryEvent
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToAbout
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToDietPreferences
import pl.handsome.club.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToProduct
import pl.handsome.club.feature.history.ui.state.HistoryState
import pl.handsome.club.feature.history.ui.state.HistoryStateFactory
import pl.handsome.club.feature.history.ui.state.HistoryStatePrototype as Prototype

internal class HistoryViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: AnalysisHistoryRepository,
    private val stateFactory: HistoryStateFactory,
    private val logger: AppLogger,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(
        Prototype(isMenuOpen = savedStateHandle.get<Boolean>(KEY_MENU_OPEN) ?: false)
    )

    val state: StateFlow<HistoryState> = statePrototype.map(stateFactory::createFor).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(statePrototype.value),
    )

    private val screenEvents = EventQueue<HistoryEvent>(viewModelScope)
    val events: Flow<HistoryEvent> = screenEvents.events

    fun onAction(action: HistoryAction) {
        when (action) {
            OnStart -> load()
            OnRetryClicked -> load()
            is OnEntryClicked -> screenEvents send NavigateToProduct(action.barcode)
            OnMenuClicked -> setMenuOpen(true)
            OnMenuDismissed -> setMenuOpen(false)
            OnDietPreferencesClicked -> navigateFromMenu(NavigateToDietPreferences)
            OnAboutClicked -> navigateFromMenu(NavigateToAbout)
        }
    }

    private fun load() {
        if (statePrototype.value.isLoading) return
        statePrototype.update { it.copy(isLoading = true, loadError = null) }

        viewModelScope.launch {
            repository.getLastUniqueEntries(Prototype.ENTRIES_LIMIT)
                .onSuccess { entries ->
                    statePrototype.update { it.copy(entries = entries, isLoading = false) }
                }
                .onFailure { error ->
                    if (statePrototype.value.entries == null)
                        logAndShowError(error)
                    else
                        logError(error)
                }
        }
    }

    private fun logError(error: AppError) {
        log(error)
        statePrototype.update { it.copy(isLoading = false) }
    }

    private fun logAndShowError(error: AppError) {
        log(error)
        statePrototype.update { it.copy(isLoading = false, loadError = error) }
    }

    private fun log(error: AppError) {
        logger.error(TAG, "Loading history failed: $error", (error as? AppError.Unknown)?.cause)
    }

    private fun setMenuOpen(isOpen: Boolean) {
        savedStateHandle[KEY_MENU_OPEN] = isOpen
        statePrototype.update { it.copy(isMenuOpen = isOpen) }
    }

    private fun navigateFromMenu(navigation: HistoryNavigation) {
        setMenuOpen(false)
        screenEvents send navigation
    }

    private companion object {
        const val TAG = "HistoryViewModel"
        const val KEY_MENU_OPEN = "menuOpen"
    }
}
