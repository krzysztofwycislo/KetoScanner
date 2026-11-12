package pl.morning.tower.karbo.feature.history.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.ErrorReporter
import pl.morning.tower.karbo.core.common.EventQueue
import pl.morning.tower.karbo.core.common.catchAppError
import pl.morning.tower.karbo.core.common.retryOnAppError
import pl.morning.tower.karbo.core.designsystem.event.AppEvent.HideKeyboard
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.feature.history.api.AnalysisHistoryRepository
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction.OnBackClicked
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction.OnEntryClicked
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction.OnFilterClicked
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction.OnQueryChanged
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction.OnRetryClicked
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction.OnSearchSubmitted
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction.OnStart
import pl.morning.tower.karbo.feature.history.ui.event.HistoryAction.OnStartScanningClicked
import pl.morning.tower.karbo.feature.history.ui.event.HistoryEvent
import pl.morning.tower.karbo.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateBack
import pl.morning.tower.karbo.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToProduct
import pl.morning.tower.karbo.feature.history.ui.event.HistoryEvent.HistoryNavigation.NavigateToScan
import pl.morning.tower.karbo.feature.history.ui.state.HistoryFilter
import pl.morning.tower.karbo.feature.history.ui.state.HistoryState
import pl.morning.tower.karbo.feature.history.ui.state.HistoryStateFactory
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import pl.morning.tower.karbo.feature.history.ui.state.HistoryStatePrototype as Prototype

internal class HistoryViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: AnalysisHistoryRepository,
    private val preferences: DietPreferencesRepository,
    private val appEvents: AppEvents,
    private val stateFactory: HistoryStateFactory,
    private val clock: Clock,
    timeZone: TimeZone,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(
        Prototype(
            query = savedStateHandle.get<String>(KEY_QUERY) ?: "",
            filter = HistoryFilter.entries.firstOrNull { it.name == savedStateHandle.get<String>(KEY_FILTER) }
                ?: HistoryFilter.ALL,
            now = clock.now(),
            timeZone = timeZone,
        )
    )

    val state: StateFlow<HistoryState> = statePrototype.map(stateFactory::createFor).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(statePrototype.value),
    )

    private val screenEvents = EventQueue<HistoryEvent>(viewModelScope)
    val events: Flow<HistoryEvent> = screenEvents.events

    private val retryClicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun onAction(action: HistoryAction) {
        when (action) {
            OnStart -> statePrototype.update { it.copy(now = clock.now()) }
            OnRetryClicked -> retry()
            is OnQueryChanged -> setQuery(action.query)
            is OnFilterClicked -> setFilter(action.filter)
            OnSearchSubmitted -> appEvents send HideKeyboard
            is OnEntryClicked -> openProduct(action.barcode)
            OnBackClicked -> screenEvents send NavigateBack
            OnStartScanningClicked -> screenEvents send NavigateToScan
        }
    }

    private fun observeEntries() {
        viewModelScope.launch {
            repository.observeLastUniqueEntries(Prototype.ENTRIES_LIMIT)
                .retryOnAppError(::onEntriesFailed)
                .collect { entries ->
                    statePrototype.update { it.copy(entries = entries, loadError = null) }
                }
        }
    }

    private suspend fun onEntriesFailed(error: AppError) {
        errorReporter.report(TAG, "Loading history failed", error)
        if (statePrototype.value.entries == null) {
            statePrototype.update { it.copy(loadError = error) }
            retryClicks.first()
        } else {
            delay(RETRY_DELAY)
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferences.observeDietPreferences()
                .catchAppError { error -> errorReporter.report(TAG, "Loading diet preferences failed", error) }
                .collect { dietPreferences ->
                    statePrototype.update { it.copy(dietPreferences = dietPreferences) }
                }
        }
    }

    private fun setQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
        statePrototype.update { it.copy(query = query) }
    }

    private fun setFilter(filter: HistoryFilter) {
        savedStateHandle[KEY_FILTER] = filter.name
        statePrototype.update { it.copy(filter = filter) }
    }

    private fun openProduct(barcode: String) {
        appEvents send HideKeyboard
        screenEvents send NavigateToProduct(barcode)
    }

    private fun retry() {
        statePrototype.update { it.copy(loadError = null) }
        retryClicks.tryEmit(Unit)
    }

    init {
        observeEntries()
        observePreferences()
    }

    companion object {
        private const val TAG = "HistoryViewModel"
        private const val KEY_QUERY = "query"
        private const val KEY_FILTER = "filter"
        val RETRY_DELAY = 5.seconds
    }
}
