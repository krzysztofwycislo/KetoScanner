package app.carbpeek.feature.preferences.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.core.common.EventQueue
import app.carbpeek.core.common.ProgressTracker
import app.carbpeek.core.common.catchAppError
import app.carbpeek.core.common.onFailure
import app.carbpeek.core.common.onSuccess
import app.carbpeek.core.designsystem.component.MessageTone
import app.carbpeek.core.designsystem.event.AppEvent.ShowMessage
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction.OnBackClicked
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction.OnOptionSelected
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction.OnRetryClicked
import app.carbpeek.feature.preferences.ui.event.DietPreferencesAction.OnSaveClicked
import app.carbpeek.feature.preferences.ui.event.DietPreferencesEvent
import app.carbpeek.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation.NavigateBack
import app.carbpeek.feature.preferences.ui.state.DietPreferencesState
import app.carbpeek.feature.preferences.ui.state.DietPreferencesStateFactory
import app.carbpeek.feature.preferences.ui.state.DietProfileOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import app.carbpeek.feature.preferences.ui.state.DietPreferencesStatePrototype as Prototype

internal class DietPreferencesViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val preferences: DietPreferencesRepository,
    private val appEvents: AppEvents,
    private val stateFactory: DietPreferencesStateFactory,
    private val resources: DietPreferencesResources,
    private val errorReporter: ErrorReporter,
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val saving = ProgressTracker()

    private val statePrototype = MutableStateFlow(
        Prototype(
            option = savedStateHandle.get<String>(KEY_OPTION)
                ?.let { name -> DietProfileOption.entries.firstOrNull { it.name == name } },
        )
    )

    val state: StateFlow<DietPreferencesState> = statePrototype.map(stateFactory::createFor).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(statePrototype.value),
    )

    private val screenEvents = EventQueue<DietPreferencesEvent>(viewModelScope)
    val events: Flow<DietPreferencesEvent> = screenEvents.events

    private val retryClicks = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun onAction(action: DietPreferencesAction) {
        when (action) {
            is OnOptionSelected -> selectOption(action.option)
            OnSaveClicked -> save()
            OnRetryClicked -> retry()
            OnBackClicked -> screenEvents send NavigateBack
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePreferences() {
        viewModelScope.launch {
            retryClicks.onStart { emit(Unit) }
                .flatMapLatest { preferences.observeDietPreferences().catchAppError(::onLoadFailed) }
                .collect(::showPreferences)
        }
    }

    private fun onLoadFailed(error: AppError) {
        errorReporter.report(tag = TAG, message = "Loading diet preferences failed", error = error)
        statePrototype.update { it.copy(loadError = error) }
    }

    private fun showPreferences(dietPreferences: DietPreferences) {
        statePrototype.update {
            it.copy(
                savedPreferences = dietPreferences,
                option = it.option ?: DietProfileOption.of(dietPreferences),
                loadError = null,
            )
        }
    }

    private fun selectOption(option: DietProfileOption) {
        savedStateHandle[KEY_OPTION] = option.name
        statePrototype.update { it.copy(option = option) }
    }

    private fun observeSaving() {
        viewModelScope.launch {
            saving.inProgress.collect { isSaving ->
                statePrototype.update { it.copy(isSaving = isSaving) }
            }
        }
    }

    private fun save() {
        val prototype = statePrototype.value
        if (saving.inProgress.value || prototype.savedPreferences == null) return
        val chosen = prototype.chosenPreferences() ?: return

        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            saving.reportWork { preferences.setDietPreferences(chosen) }
                .onSuccess {
                    appEvents send ShowMessage(resources.saved(), MessageTone.Success)
                    screenEvents send NavigateBack
                }
                .onFailure { error ->
                    errorReporter.report(tag = TAG, message = "Saving diet preferences failed", error = error)
                    appEvents send ShowMessage(resources.saveFailed(error), MessageTone.Error)
                }
        }
    }

    private fun retry() {
        statePrototype.update { it.copy(loadError = null) }
        retryClicks.tryEmit(Unit)
    }

    init {
        observePreferences()
        observeSaving()
    }

    private companion object {
        const val TAG = "DietPreferencesViewModel"
        const val KEY_OPTION = "option"
    }
}
