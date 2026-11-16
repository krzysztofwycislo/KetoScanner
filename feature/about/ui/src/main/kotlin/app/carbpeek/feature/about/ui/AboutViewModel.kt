package app.carbpeek.feature.about.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.carbpeek.core.common.EventQueue
import app.carbpeek.feature.about.ui.event.AboutAction
import app.carbpeek.feature.about.ui.event.AboutAction.OnBackClicked
import app.carbpeek.feature.about.ui.event.AboutEvent
import app.carbpeek.feature.about.ui.event.AboutEvent.AboutNavigation.NavigateBack
import kotlinx.coroutines.flow.Flow

internal class AboutViewModel : ViewModel() {

    private val screenEvents = EventQueue<AboutEvent>(viewModelScope)
    val events: Flow<AboutEvent> = screenEvents.events

    fun onAction(action: AboutAction) {
        when (action) {
            OnBackClicked -> screenEvents send NavigateBack
        }
    }
}
