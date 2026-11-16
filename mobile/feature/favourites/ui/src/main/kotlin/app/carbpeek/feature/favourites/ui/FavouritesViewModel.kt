package app.carbpeek.feature.favourites.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.core.common.EventQueue
import app.carbpeek.core.common.catchAppError
import app.carbpeek.core.designsystem.event.AppEvent.HideKeyboard
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.favourites.api.FavouriteProductsRepository
import app.carbpeek.feature.favourites.ui.event.FavouritesAction
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnGroupClicked
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnItemClicked
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnQueryChanged
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnRetryClicked
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnSearchSubmitted
import app.carbpeek.feature.favourites.ui.event.FavouritesAction.OnStartScanningClicked
import app.carbpeek.feature.favourites.ui.event.FavouritesEvent
import app.carbpeek.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation.NavigateToProduct
import app.carbpeek.feature.favourites.ui.event.FavouritesEvent.FavouritesNavigation.NavigateToScan
import app.carbpeek.feature.favourites.ui.state.FavouritesState
import app.carbpeek.feature.favourites.ui.state.FavouritesStateFactory
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
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
import app.carbpeek.feature.favourites.ui.state.FavouritesStatePrototype as Prototype

internal class FavouritesViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val favourites: FavouriteProductsRepository,
    private val preferences: DietPreferencesRepository,
    private val appEvents: AppEvents,
    private val stateFactory: FavouritesStateFactory,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    private val statePrototype = MutableStateFlow(
        Prototype(
            query = savedStateHandle.get<String>(KEY_QUERY) ?: "",
            collapsed = savedStateHandle.get<ArrayList<String>>(KEY_COLLAPSED)
                .orEmpty()
                .mapNotNull { name -> ProductCategory.entries.firstOrNull { it.name == name } }
                .toSet(),
        )
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
            is OnGroupClicked -> toggleGroup(action.category)
            OnStartScanningClicked -> screenEvents send NavigateToScan
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeFavourites() {
        viewModelScope.launch {
            combine(
                statePrototype.map { it.query.trim() }.distinctUntilChanged(),
                retryClicks.onStart { emit(Unit) },
            ) { query, _ -> query }
                .flatMapLatest { query ->
                    favourites.observeFavourites(query)
                        .map { query to it }
                        .catchAppError(::onFavouritesFailed)
                }
                .collect { (query, list) ->
                    statePrototype.update {
                        it.copy(favourites = list, favouritesQuery = query, loadError = null)
                    }
                }
        }
    }

    private fun onFavouritesFailed(error: AppError) {
        errorReporter.report(tag = TAG, message = "Loading favourites failed", error = error)
        statePrototype.update { it.copy(loadError = error) }
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

    private fun setQuery(query: String) {
        savedStateHandle[KEY_QUERY] = query
        statePrototype.update { it.copy(query = query) }
    }

    private fun toggleGroup(category: ProductCategory) {
        val prototype = statePrototype.value
        if (prototype.favouritesQuery.isNotBlank()) return
        val collapsed = if (category in prototype.collapsed) {
            prototype.collapsed - category
        } else {
            prototype.collapsed + category
        }
        savedStateHandle[KEY_COLLAPSED] = ArrayList(collapsed.map { it.name })
        statePrototype.update { it.copy(collapsed = collapsed) }
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
        const val KEY_COLLAPSED = "collapsed"
    }
}
