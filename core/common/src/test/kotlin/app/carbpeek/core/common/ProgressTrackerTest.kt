package app.carbpeek.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ProgressTrackerTest {

    @Test
    fun `when created then nothing is in progress`() {
        assertFalse(ProgressTracker().inProgress.value)
    }

    @Test
    fun `when a block is suspended then work is in progress until it completes`() = runTest {
        val tracker = ProgressTracker()
        val gate = CompletableDeferred<Unit>()

        launch { tracker.reportWork { gate.await() } }
        runCurrent()
        assertTrue(tracker.inProgress.value)

        gate.complete(Unit)
        runCurrent()
        assertFalse(tracker.inProgress.value)
    }

    @Test
    fun `when a block returns a value then report work returns it`() = runTest {
        assertEquals(VALUE, ProgressTracker().reportWork { VALUE })
    }

    @Test
    fun `when a block throws then the same exception is rethrown and nothing is in progress`() = runTest {
        val tracker = ProgressTracker()
        val failure = IllegalStateException("boom")

        val thrown = assertFailsWith<IllegalStateException> { tracker.reportWork { throw failure } }

        assertSame(failure, thrown)
        assertFalse(tracker.inProgress.value)
    }

    @Test
    fun `when the job is cancelled then cancellation propagates and nothing is in progress`() = runTest {
        val tracker = ProgressTracker()
        var caught: Throwable? = null
        val job = launch {
            try {
                tracker.reportWork { CompletableDeferred<Unit>().await() }
            } catch (e: CancellationException) {
                caught = e
                throw e
            }
        }
        runCurrent()

        job.cancel()
        runCurrent()

        assertTrue(caught is CancellationException)
        assertFalse(tracker.inProgress.value)
    }

    @Test
    fun `when two works overlap then progress lasts until the second one finishes`() = runTest {
        val tracker = ProgressTracker()
        val first = CompletableDeferred<Unit>()
        val second = CompletableDeferred<Unit>()
        launch { tracker.reportWork { first.await() } }
        launch { tracker.reportWork { second.await() } }
        runCurrent()

        first.complete(Unit)
        runCurrent()
        assertTrue(tracker.inProgress.value)

        second.complete(Unit)
        runCurrent()
        assertFalse(tracker.inProgress.value)
    }

    private companion object {
        const val VALUE = 42
    }
}
