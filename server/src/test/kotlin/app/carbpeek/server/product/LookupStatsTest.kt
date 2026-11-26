package app.carbpeek.server.product

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class LookupStatsTest {

    private val stats = LookupStats()
    private val lines = mutableListOf<String>()
    private val statsLogger = LookupStatsLogger(stats = stats, log = { lines += it })

    @Test
    fun `when events are recorded then they are counted and taking the counts resets them`() {
        stats.record(LookupEvent.FRESH_HIT)
        stats.record(LookupEvent.FRESH_HIT)
        stats.record(LookupEvent.OFF_CALL)

        val first = stats.takeCounts()
        val second = stats.takeCounts()

        assertEquals(expected = LookupEvent.entries.toSet(), actual = first.keys)
        assertEquals(
            expected = mapOf(LookupEvent.FRESH_HIT to 2L, LookupEvent.OFF_CALL to 1L),
            actual = first.filterValues { it > 0 },
        )
        assertEquals(expected = emptyMap(), actual = second.filterValues { it > 0 })
    }

    @Test
    fun `when 15 minutes pass then one line has all counts of these minutes`() =
        runTest {
            backgroundScope.launch { statsLogger.run() }
            runCurrent()
            stats.record(LookupEvent.MISS)
            stats.record(LookupEvent.OFF_CALL)

            advanceTimeBy(15.minutes - 1.seconds)
            assertEquals(expected = emptyList(), actual = lines)
            advanceTimeBy(2.seconds)
            stats.record(LookupEvent.FRESH_HIT)
            advanceTimeBy(15.minutes)

            assertEquals(
                expected = listOf(
                    "Product lookups: FRESH_HIT=0 STALE_HIT=0 KNOWN_MISSING=0 MISS=1 OFF_CALL=1 OFF_FAILURE=0 " +
                        "OFF_LIMIT_REACHED=0 DATABASE_FAILURE=0",
                    "Product lookups: FRESH_HIT=1 STALE_HIT=0 KNOWN_MISSING=0 MISS=0 OFF_CALL=0 OFF_FAILURE=0 " +
                        "OFF_LIMIT_REACHED=0 DATABASE_FAILURE=0",
                ),
                actual = lines,
            )
        }

    @Test
    fun `when nothing happened then nothing is logged`() =
        runTest {
            val job = backgroundScope.launch { statsLogger.run() }
            runCurrent()

            advanceTimeBy(60.minutes)
            job.cancelAndJoin()

            assertEquals(expected = emptyList(), actual = lines)
        }

    @Test
    fun `when the logger is cancelled then it logs the counts so far`() =
        runTest {
            val job = backgroundScope.launch { statsLogger.run() }
            runCurrent()
            stats.record(LookupEvent.STALE_HIT)

            job.cancelAndJoin()

            assertEquals(
                expected = listOf(
                    "Product lookups: FRESH_HIT=0 STALE_HIT=1 KNOWN_MISSING=0 MISS=0 OFF_CALL=0 OFF_FAILURE=0 " +
                        "OFF_LIMIT_REACHED=0 DATABASE_FAILURE=0",
                ),
                actual = lines,
            )
        }
}
