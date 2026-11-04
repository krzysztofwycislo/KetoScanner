package pl.handsome.club.core.designsystem.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import pl.handsome.club.core.common.EventQueue

class AppEvents(scope: CoroutineScope) {

    private val queue = EventQueue<AppEvent>(scope)

    val events: Flow<AppEvent> = queue.events

    infix fun send(event: AppEvent) = queue send event
}
