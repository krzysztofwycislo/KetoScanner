package pl.handsome.club.feature.about.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import pl.handsome.club.core.common.EventQueue
import pl.handsome.club.feature.about.ui.event.AboutAction
import pl.handsome.club.feature.about.ui.event.AboutAction.OnBackClicked
import pl.handsome.club.feature.about.ui.event.AboutEvent
import pl.handsome.club.feature.about.ui.event.AboutEvent.AboutNavigation.NavigateBack

internal class AboutViewModel : ViewModel() {

    private val screenEvents = EventQueue<AboutEvent>(viewModelScope)
    val events: Flow<AboutEvent> = screenEvents.events

    fun onAction(action: AboutAction) {
        when (action) {
            OnBackClicked -> screenEvents send NavigateBack
        }
    }
}
