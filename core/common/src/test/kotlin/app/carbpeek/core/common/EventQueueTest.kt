package app.carbpeek.core.common

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EventQueueTest {

    @Test
    fun `when event is sent before anyone collects then a later collector receives it`() = runTest {
        val queue = EventQueue<String>(backgroundScope)

        queue send FIRST

        assertEquals(FIRST, queue.events.first())
    }

    @Test
    fun `when events are sent from non suspend code then they arrive in order`() = runTest {
        val queue = EventQueue<String>(backgroundScope)

        sendAll(queue, FIRST, SECOND, THIRD)

        assertEquals(listOf(FIRST, SECOND, THIRD), queue.events.take(3).toList())
    }

    @Test
    fun `when an event is received then the next collection gets the next event`() = runTest {
        val queue = EventQueue<String>(backgroundScope)
        sendAll(queue, FIRST, SECOND)

        assertEquals(FIRST, queue.events.first())
        assertEquals(SECOND, queue.events.first())
    }

    private fun sendAll(queue: EventQueue<String>, vararg events: String) {
        events.forEach { queue send it }
    }

    private companion object {
        const val FIRST = "first"
        const val SECOND = "second"
        const val THIRD = "third"
    }
}
