package pl.morning.tower.karbo.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class EventQueue<T>(private val scope: CoroutineScope) {

    private val channel = Channel<T>(Channel.BUFFERED)

    val events: Flow<T> = channel.receiveAsFlow()

    infix fun send(event: T) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) { channel.send(event) }
    }
}
