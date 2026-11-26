package app.carbpeek.server.product

import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

private val WINDOW = 60.seconds
private val SHORTEST_WAIT = 1.seconds

internal class RequestBudget(
    private val limit: Int,
    private val timeSource: TimeSource.WithComparableMarks,
) {
    private val used = ArrayDeque<ComparableTimeMark>()

    @Synchronized
    fun tryAcquire(reserved: Int = 0): Duration? {
        while (used.isNotEmpty() && used.first().elapsedNow() >= WINDOW) {
            used.removeFirst()
        }
        val available = limit - reserved
        if (used.size < available) {
            used.addLast(timeSource.markNow())
            return null
        }
        val blocking = used[used.size - available]
        return maxOf(WINDOW - blocking.elapsedNow(), SHORTEST_WAIT)
    }
}
