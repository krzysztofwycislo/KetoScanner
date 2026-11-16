package app.carbpeek.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.core.common.EventQueue
import app.carbpeek.core.common.catchAppError
import app.carbpeek.core.common.retryOnAppError
import app.carbpeek.feature.history.api.AnalysisHistoryRepository
import app.carbpeek.feature.home.ui.event.HomeAction
import app.carbpeek.feature.home.ui.event.HomeAction.OnAboutClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnEntryClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnProfileClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnRetryClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnSeeAllClicked
import app.carbpeek.feature.home.ui.event.HomeAction.OnStart
import app.carbpeek.feature.home.ui.event.HomeAction.OnStartScanningClicked
import app.carbpeek.feature.home.ui.event.HomeEvent
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToAbout
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToDietPreferences
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToHistory
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToProduct
import app.carbpeek.feature.home.ui.event.HomeEvent.HomeNavigation.NavigateToScan
import app.carbpeek.feature.home.ui.state.HomeState
import app.carbpeek.feature.home.ui.state.HomeStateFactory
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import app.carbpeek.feature.home.ui.state.HomeStatePrototype as Prototype

internal class HomeViewModel(
    private val history: AnalysisHistoryRepository,
    private val preferences: DietPreferencesRepository,
    private val stateFactory: HomeStateFactory,
    private val clock: Clock,
    timeZone: TimeZone,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(Prototype(now = clock.now(), timeZone = timeZone))

    val state: StateFlow<HomeState> = statePrototype.map(stateFactory::createFor).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(statePrototype.value),
    )

    private val screenEvents = EventQueue<HomeEvent>(viewModelScope)
    val events: Flow<HomeEvent> = screenEvents.events

    private val retryClicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun onAction(action: HomeAction) {
        when (action) {
            OnStart -> statePrototype.update { it.copy(now = clock.now()) }
            OnRetryClicked -> retry()
            OnProfileClicked -> screenEvents send NavigateToDietPreferences
            OnAboutClicked -> screenEvents send NavigateToAbout
            OnStartScanningClicked -> screenEvents send NavigateToScan
            OnSeeAllClicked -> screenEvents send NavigateToHistory
            is OnEntryClicked -> screenEvents send NavigateToProduct(action.barcode)
        }
    }

    private fun observeRecentScans() {
        viewModelScope.launch {
            history.observeLastUniqueEntries(Prototype.RECENT_LIMIT)
                .retryOnAppError(::onRecentScansFailed)
                .collect { entries ->
                    statePrototype.update { it.copy(entries = entries, loadError = null) }
                }
        }
    }

    private suspend fun onRecentScansFailed(error: AppError) {
        errorReporter.report(tag = TAG, message = "Loading recent scans failed", error = error)
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
                .catchAppError { error ->
                    errorReporter.report(
                        tag = TAG,
                        message = "Loading diet preferences failed",
                        error = error,
                    )
                }
                .collect { dietPreferences ->
                    statePrototype.update { it.copy(dietPreferences = dietPreferences) }
                }
        }
    }

    private fun retry() {
        statePrototype.update { it.copy(loadError = null) }
        retryClicks.tryEmit(Unit)
    }

    init {
        observeRecentScans()
        observePreferences()
    }

    companion object {
        private const val TAG = "HomeViewModel"
        val RETRY_DELAY = 5.seconds
    }
}
