package app.carbpeek.feature.onboarding.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.core.common.EventQueue
import app.carbpeek.core.common.ProgressTracker
import app.carbpeek.core.common.onFailure
import app.carbpeek.core.common.onSuccess
import app.carbpeek.core.designsystem.component.MessageTone
import app.carbpeek.core.designsystem.event.AppEvent.ShowMessage
import app.carbpeek.core.designsystem.event.AppEvents
import app.carbpeek.feature.onboarding.ui.event.OnboardingAction
import app.carbpeek.feature.onboarding.ui.event.OnboardingAction.OnPresetSelected
import app.carbpeek.feature.onboarding.ui.event.OnboardingAction.OnSkipClicked
import app.carbpeek.feature.onboarding.ui.event.OnboardingAction.OnStartScanningClicked
import app.carbpeek.feature.onboarding.ui.event.OnboardingEvent
import app.carbpeek.feature.onboarding.ui.event.OnboardingEvent.OnboardingNavigation.NavigateToScan
import app.carbpeek.feature.onboarding.ui.state.OnboardingState
import app.carbpeek.feature.onboarding.ui.state.OnboardingStateFactory
import app.carbpeek.feature.preferences.api.OnboardingRepository
import app.carbpeek.feature.preferences.api.model.DietPreset
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import app.carbpeek.feature.onboarding.ui.state.OnboardingStatePrototype as Prototype

internal class OnboardingViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val onboarding: OnboardingRepository,
    private val appEvents: AppEvents,
    private val stateFactory: OnboardingStateFactory,
    private val resources: OnboardingResources,
    private val errorReporter: ErrorReporter,
) : ViewModel() {

    private val saving = ProgressTracker()

    private val statePrototype = MutableStateFlow(
        Prototype(
            selectedPreset = savedStateHandle.get<String>(KEY_PRESET)
                ?.let { name -> DietPreset.entries.firstOrNull { it.name == name } }
                ?: DietPreset.KETO,
        ),
    )

    val state: StateFlow<OnboardingState> = statePrototype.map(stateFactory::createFor).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = stateFactory.createFor(statePrototype.value),
    )

    private val screenEvents = EventQueue<OnboardingEvent>(viewModelScope)
    val events: Flow<OnboardingEvent> = screenEvents.events

    fun onAction(action: OnboardingAction) {
        when (action) {
            is OnPresetSelected -> selectPreset(action.preset)
            OnStartScanningClicked -> completeSetup(statePrototype.value.selectedPreset)
            OnSkipClicked -> completeSetup(DietPreset.KETO)
        }
    }

    private fun selectPreset(preset: DietPreset) {
        savedStateHandle[KEY_PRESET] = preset.name
        statePrototype.update { it.copy(selectedPreset = preset) }
    }

    private fun observeSaving() {
        viewModelScope.launch {
            saving.inProgress.collect { isSaving ->
                statePrototype.update { it.copy(isSaving = isSaving) }
            }
        }
    }

    private fun completeSetup(preset: DietPreset) {
        if (saving.inProgress.value) return
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            saving.reportWork { onboarding.completeSetup(preset) }
                .onSuccess { screenEvents send NavigateToScan }
                .onFailure { error ->
                    errorReporter.report(tag = TAG, message = "Completing setup failed", error = error)
                    appEvents send ShowMessage(resources.saveFailed(error), MessageTone.Error)
                }
        }
    }

    init {
        observeSaving()
    }

    private companion object {
        const val TAG = "OnboardingViewModel"
        const val KEY_PRESET = "preset"
    }
}
