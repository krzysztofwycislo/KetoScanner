package app.carbpeek.server.product

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.LongAdder
import kotlin.time.Duration.Companion.minutes

private val LOG_INTERVAL = 15.minutes

internal enum class LookupEvent {
    FRESH_HIT,
    STALE_HIT,
    KNOWN_MISSING,
    MISS,
    OFF_CALL,
    OFF_FAILURE,
    OFF_LIMIT_REACHED,
    DATABASE_FAILURE,
}

internal class LookupStats {
    private val counters = LookupEvent.entries.associateWith { LongAdder() }

    fun record(event: LookupEvent) {
        counters.getValue(event).increment()
    }

    fun takeCounts(): Map<LookupEvent, Long> = counters.mapValues { (_, counter) -> counter.sumThenReset() }
}

internal class LookupStatsLogger(
    private val stats: LookupStats,
    private val log: (String) -> Unit = { LoggerFactory.getLogger(LookupStatsLogger::class.java).info(it) },
) {
    suspend fun run(): Nothing {
        try {
            while (true) {
                delay(LOG_INTERVAL)
                logCounts()
            }
        } finally {
            logCounts()
        }
    }

    private fun logCounts() {
        val counts = stats.takeCounts()
        if (counts.values.any { it > 0 }) {
            log(
                counts.entries.joinToString(
                    separator = " ",
                    prefix = "Product lookups: ",
                ) { (event, count) -> "${event.name}=$count" },
            )
        }
    }
}
