package pl.morning.tower.karbo.feature.preferences.ui

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
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.ErrorReporter
import pl.morning.tower.karbo.core.common.EventQueue
import pl.morning.tower.karbo.core.common.ProgressTracker
import pl.morning.tower.karbo.core.common.catchAppError
import pl.morning.tower.karbo.core.common.onFailure
import pl.morning.tower.karbo.core.common.onSuccess
import pl.morning.tower.karbo.core.designsystem.component.MessageTone
import pl.morning.tower.karbo.core.designsystem.event.AppEvent.HideKeyboard
import pl.morning.tower.karbo.core.designsystem.event.AppEvent.ShowMessage
import pl.morning.tower.karbo.core.designsystem.event.AppEvents
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesAction
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesAction.OnBackClicked
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesAction.OnCustomModeSelected
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesAction.OnInputChanged
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesAction.OnOptionSelected
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesAction.OnRetryClicked
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesAction.OnSaveClicked
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesEvent
import pl.morning.tower.karbo.feature.preferences.ui.event.DietPreferencesEvent.DietPreferencesNavigation.NavigateBack
import pl.morning.tower.karbo.feature.preferences.ui.state.DietPreferencesState
import pl.morning.tower.karbo.feature.preferences.ui.state.DietPreferencesStateFactory
import pl.morning.tower.karbo.feature.preferences.ui.state.DietProfileOption
import pl.morning.tower.karbo.feature.preferences.ui.state.DietPreferencesStatePrototype as Prototype

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
            input = savedStateHandle.get<String>(KEY_INPUT),
            customMode = savedStateHandle.get<String>(KEY_CUSTOM_MODE)
                ?.let { name -> DietMode.entries.firstOrNull { it.name == name } },
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
            is OnCustomModeSelected -> selectCustomMode(action.mode)
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
                input = it.input ?: dietPreferences.maxDailyCarbsAmount.toString(),
                customMode = it.customMode ?: dietPreferences.dietMode,
                loadError = null,
            )
        }
    }

    private fun selectOption(option: DietProfileOption) {
        savedStateHandle[KEY_OPTION] = option.name
        statePrototype.update { it.copy(option = option, isInputInvalid = false) }
    }

    private fun selectCustomMode(mode: DietMode) {
        savedStateHandle[KEY_CUSTOM_MODE] = mode.name
        statePrototype.update { it.copy(customMode = mode, isInputInvalid = false) }
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
        val prototype = statePrototype.value
        if (saving.inProgress.value || prototype.savedPreferences == null) return
        val chosen = prototype.chosenPreferences()
        if (chosen == null) {
            statePrototype.update { it.copy(isInputInvalid = true) }
            return
        }

        statePrototype.update { it.copy(isInputInvalid = false) }
        appEvents send HideKeyboard
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
        const val KEY_OPTION = "option"
        const val KEY_INPUT = "input"
        const val KEY_CUSTOM_MODE = "customMode"
        const val MAX_INPUT_LENGTH = 3
    }
}
