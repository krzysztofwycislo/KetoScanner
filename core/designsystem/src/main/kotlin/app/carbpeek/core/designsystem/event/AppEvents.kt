package app.carbpeek.core.designsystem.event

import app.carbpeek.core.common.EventQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class AppEvents(scope: CoroutineScope) {

    private val queue = EventQueue<AppEvent>(scope)

    val events: Flow<AppEvent> = queue.events

    infix fun send(event: AppEvent) = queue send event
}
