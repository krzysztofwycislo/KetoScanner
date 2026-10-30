package pl.handsome.club.feature.favourites.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.handsome.club.core.common.ErrorReporter
import pl.handsome.club.core.common.EventQueue
import pl.handsome.club.core.common.onFailure
import pl.handsome.club.core.common.onSuccess
import pl.handsome.club.core.designsystem.event.AppEvent.HideKeyboard
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.feature.favourites.api.FavouriteProductsRepository
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnItemClicked
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnQueryChanged
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnRetryClicked
import pl.handsome.club.feature.favourites.ui.event.FavouritesAction.OnSearchSubmitted
import pl.handsome.club.feature.favourites.ui.event.FavouritesEvent
import pl.handsome.club.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation.NavigateToProduct
import pl.handsome.club.feature.favourites.ui.state.FavouritesState
import pl.handsome.club.feature.favourites.ui.state.FavouritesStateFactory
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.favourites.ui.state.FavouritesStatePrototype as Prototype

internal class FavouritesViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val favourites: FavouriteProductsRepository,
    private val preferences: DietPreferencesRepository,
    private val appEvents: AppEvents,
    private val stateFactory: FavouritesStateFactory,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(
        Prototype(query = savedStateHandle.get<String>(KEY_QUERY) ?: "")
    )

    val state: StateFlow<FavouritesState> = statePrototype.map(stateFactory::createFor).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(statePrototype.value),
    )

    private val screenEvents = EventQueue<FavouritesEvent>(viewModelScope)
    val events: Flow<FavouritesEvent> = screenEvents.events

    private val retryClicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun onAction(action: FavouritesAction) {
        when (action) {
            is OnQueryChanged -> setQuery(action.query)
            OnSearchSubmitted -> appEvents send HideKeyboard
            is OnItemClicked -> openProduct(action.barcode)
            OnRetryClicked -> retry()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeFavourites() {
        viewModelScope.launch {
            combine(
                statePrototype.map { it.query.trim() }.distinctUntilChanged(),
                retryClicks.onStart { emit(Unit) },
            ) { query, _ -> query }
                .flatMapLatest { query -> favourites.observeFavourites(query).map { query to it } }
                .collect { (query, outcome) ->
                    outcome
                        .onSuccess { list ->
                            statePrototype.update {
                                it.copy(favourites = list, favouritesQuery = query, loadError = null)
                            }
                        }
                        .onFailure { error ->
                            errorReporter.report(TAG, "Loading favourites failed", error)
                            statePrototype.update { it.copy(loadError = error) }
                        }
                }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferences.observeDietPreferences().collect { outcome ->
                outcome
                    .onSuccess { dietPreferences ->
                        statePrototype.update { it.copy(dietPreferences = dietPreferences) }
                    }
                    .onFailure { error -> errorReporter.report(TAG, "Loading diet preferences failed", error) }
            }
        }
    }

    private fun setQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
        statePrototype.update { it.copy(query = query) }
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
        observeFavourites()
        observePreferences()
    }

    private companion object {
        const val TAG = "FavouritesViewModel"
        const val KEY_QUERY = "query"
    }
}
