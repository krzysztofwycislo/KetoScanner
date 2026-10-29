package pl.handsome.club.feature.preferences.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import pl.handsome.club.core.common.ErrorReporter
import pl.handsome.club.core.common.EventQueue
import pl.handsome.club.core.common.ProgressTracker
import pl.handsome.club.core.common.onFailure
import pl.handsome.club.core.common.onSuccess
import pl.handsome.club.core.designsystem.event.AppEvent.HideKeyboard
import pl.handsome.club.core.designsystem.event.AppEvent.ShowMessage
import pl.handsome.club.core.designsystem.event.AppEvents
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnBackClicked
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnDietModeSelected
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnInputChanged
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnRetryClicked
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesAction.OnSaveClicked
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesEvent
import pl.handsome.club.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation.NavigateBack
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesState
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStateFactory
import pl.handsome.club.feature.preferences.ui.state.DietPreferencesStatePrototype as Prototype

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
        Prototype(input = savedStateHandle.get<String>(KEY_INPUT))
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
            is OnDietModeSelected -> selectDietMode(action.mode)
            is OnInputChanged -> setInput(action.text)
            OnSaveClicked -> save()
            OnRetryClicked -> retry()
            OnBackClicked -> navigateBack()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observePreferences() {
        viewModelScope.launch {
            retryClicks.onStart { emit(Unit) }
                .flatMapLatest { preferences.observeDietPreferences() }
                .collect { outcome ->
                    outcome
                        .onSuccess(::showPreferences)
                        .onFailure { error ->
                            errorReporter.report(TAG, "Loading diet preferences failed", error)
                            statePrototype.update { it.copy(loadError = error) }
                        }
                }
        }
    }

    private fun showPreferences(dietPreferences: DietPreferences) {
        val amount = dietPreferences.maxDailyCarbsAmount
        statePrototype.update {
            it.copy(
                savedAmount = amount,
                savedMode = dietPreferences.dietMode,
                input = it.input ?: amount.toString(),
                loadError = null,
            )
        }
    }

    private fun selectDietMode(mode: DietMode) {
        applicationScope.launch {
            preferences.setDietMode(mode)
                .onFailure { error ->
                    errorReporter.report(TAG, "Saving diet mode failed", error)
                    appEvents send ShowMessage(resources.saveFailed(error))
                }
        }
    }

    private fun setInput(text: String) {
        val input = text.filter { it in '0'..'9' }.take(MAX_INPUT_LENGTH)
        savedStateHandle[KEY_INPUT] = input
        statePrototype.update { it.copy(input = input) }
    }

    private fun observeSaving() {
        viewModelScope.launch {
            saving.inProgress.collect { isSaving ->
                statePrototype.update { it.copy(isSaving = isSaving) }
            }
        }
    }

    private fun save() {
        if (saving.inProgress.value) return
        val amount = statePrototype.value.input?.trim()?.toIntOrNull()
            ?.takeIf { it in DietPreferences.MAX_DAILY_CARBS_RANGE }
        if (amount == null) {
            statePrototype.update { it.copy(isInputInvalid = true) }
            return
        }

        statePrototype.update { it.copy(isInputInvalid = false) }
        appEvents send HideKeyboard
        applicationScope.launch(start = CoroutineStart.UNDISPATCHED) {
            saving.reportWork { preferences.setMaxDailyCarbsAmount(amount) }
                .onSuccess { appEvents send ShowMessage(resources.saved()) }
                .onFailure { error ->
                    errorReporter.report(TAG, "Saving diet preferences failed", error)
                    appEvents send ShowMessage(resources.saveFailed(error))
                }
        }
    }

    private fun retry() {
        statePrototype.update { it.copy(loadError = null) }
        retryClicks.tryEmit(Unit)
    }

    private fun navigateBack() {
        appEvents send HideKeyboard
        screenEvents send NavigateBack
    }

    init {
        observePreferences()
        observeSaving()
    }

    private companion object {
        const val TAG = "DietPreferencesViewModel"
        const val KEY_INPUT = "input"
        const val MAX_INPUT_LENGTH = 3
    }
}
