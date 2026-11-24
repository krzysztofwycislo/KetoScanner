package app.carbpeek.core.designsystem.component

import coil3.network.HttpException
import coil3.network.NetworkResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class RetryingImageStateTest {

    private val state = RetryingImageState()

    @Test
    fun `a new state loads the first attempt`() {
        assertEquals(load(attempt = 0, status = ImageLoadStatus.LOADING), state.load.value)
    }

    @Test
    fun `a loaded photo is loaded and never tries again`() = runTest {
        startRetrying(state)
        state.onLoaded(attempt = 0)

        advance(1.hours)

        assertEquals(load(attempt = 0, status = ImageLoadStatus.LOADED), state.load.value)
    }

    @Test
    fun `failures are tried again after 5, 15 and 45 seconds`() = runTest {
        startRetrying(state)

        listOf(5.seconds, 15.seconds, 45.seconds).forEachIndexed { index, pause ->
            state.onFailed(attempt = index, error = IOException())

            advance(pause - 1.milliseconds)
            assertEquals(
                load(attempt = index, automaticRetries = index, status = ImageLoadStatus.FAILED),
                state.load.value,
            )

            advance(1.milliseconds)
            assertEquals(
                load(attempt = index + 1, automaticRetries = index + 1, status = ImageLoadStatus.LOADING),
                state.load.value,
            )
        }
    }

    @Test
    fun `after three automatic tries another failure starts nothing`() = runTest {
        startRetrying(state)
        repeat(3) { attempt ->
            state.onFailed(attempt = attempt, error = IOException())
            advance(45.seconds)
        }
        state.onFailed(attempt = 3, error = IOException())

        advance(1.hours)

        assertEquals(load(attempt = 3, automaticRetries = 3, status = ImageLoadStatus.FAILED), state.load.value)
    }

    @Test
    fun `a state with three used tries never tries again by itself`() = runTest {
        val usedUp = RetryingImageState(automaticRetries = 3)
        startRetrying(usedUp)
        usedUp.onFailed(attempt = 0, error = IOException())

        advance(1.hours)

        assertEquals(load(attempt = 0, automaticRetries = 3, status = ImageLoadStatus.FAILED), usedUp.load.value)
    }

    @Test
    fun `while it is stopped a failure waits for the next start, which tries once without using a try`() = runTest {
        val retrying = startRetrying(state)
        state.onFailed(attempt = 0, error = IOException())
        advance(1.seconds)
        stopRetrying(retrying)

        advance(1.hours)
        assertEquals(load(attempt = 0, status = ImageLoadStatus.FAILED), state.load.value)

        startRetrying(state)
        assertEquals(load(attempt = 1, status = ImageLoadStatus.LOADING), state.load.value)

        advance(1.hours)
        assertEquals(load(attempt = 1, status = ImageLoadStatus.LOADING), state.load.value)
    }

    @Test
    fun `a start after a failure tries exactly once when the automatic tries are used up`() = runTest {
        val usedUp = RetryingImageState(automaticRetries = 3)
        usedUp.onFailed(attempt = 0, error = IOException())

        startRetrying(usedUp)
        assertEquals(load(attempt = 1, automaticRetries = 3, status = ImageLoadStatus.LOADING), usedUp.load.value)

        usedUp.onFailed(attempt = 1, error = IOException())
        advance(1.hours)
        assertEquals(load(attempt = 1, automaticRetries = 3, status = ImageLoadStatus.FAILED), usedUp.load.value)
    }

    @Test
    fun `a start while loading or loaded starts no attempt`() = runTest {
        stopRetrying(startRetrying(state))
        assertEquals(load(attempt = 0, status = ImageLoadStatus.LOADING), state.load.value)

        state.onLoaded(attempt = 0)
        startRetrying(state)
        assertEquals(load(attempt = 0, status = ImageLoadStatus.LOADED), state.load.value)
    }

    @Test
    fun `a photo that does not exist is missing and neither time, a start nor a retry tries again`() = runTest {
        listOf(404, 410).forEach { code ->
            val missing = RetryingImageState()
            val retrying = startRetrying(missing)
            missing.onFailed(attempt = 0, error = httpError(code))

            advance(1.hours)
            stopRetrying(retrying)
            startRetrying(missing)
            missing.retry()
            advance(1.hours)

            assertEquals(load(attempt = 0, status = ImageLoadStatus.MISSING), missing.load.value, "HTTP $code")
        }
    }

    @Test
    fun `every other error is a failure`() {
        val errors = listOf(504, 503, 500, 429, 403).map(::httpError) + IOException() + IllegalStateException()

        errors.forEach { error ->
            assertEquals(ImageLoadStatus.FAILED, imageFailureStatusOf(error), "$error")
        }
    }

    @Test
    fun `reload starts an attempt while loading, after a failure and for a missing photo`() {
        val loading = RetryingImageState()
        val failed = RetryingImageState().apply { onFailed(attempt = 0, error = IOException()) }
        val missing = RetryingImageState().apply { onFailed(attempt = 0, error = httpError(404)) }

        listOf(loading, failed, missing).forEach { state ->
            state.reload()

            assertEquals(load(attempt = 1, status = ImageLoadStatus.LOADING), state.load.value)
        }
    }

    @Test
    fun `reload keeps a loaded photo`() {
        state.onLoaded(attempt = 0)

        state.reload()

        assertEquals(load(attempt = 0, status = ImageLoadStatus.LOADED), state.load.value)
    }

    @Test
    fun `results of an older attempt are ignored`() {
        state.reload()

        state.onLoaded(attempt = 0)
        assertEquals(load(attempt = 1, status = ImageLoadStatus.LOADING), state.load.value)

        state.onFailed(attempt = 0, error = IOException())
        assertEquals(load(attempt = 1, status = ImageLoadStatus.LOADING), state.load.value)
    }

    @Test
    fun `retry after a failure starts one attempt and a second call while loading none`() {
        state.onFailed(attempt = 0, error = IOException())

        state.retry()
        state.retry()

        assertEquals(load(attempt = 1, status = ImageLoadStatus.LOADING), state.load.value)
    }

    private fun load(attempt: Int, status: ImageLoadStatus, automaticRetries: Int = 0) = ImageLoad(
        attempt = attempt,
        automaticRetries = automaticRetries,
        status = status,
    )

    private fun httpError(code: Int) = HttpException(NetworkResponse(code = code))

    private fun TestScope.startRetrying(state: RetryingImageState): Job {
        val job = backgroundScope.launch { state.retryWhileActive() }
        runCurrent()
        return job
    }

    private fun TestScope.stopRetrying(job: Job) {
        job.cancel()
        runCurrent()
    }

    private fun TestScope.advance(duration: Duration) {
        runCurrent()
        advanceTimeBy(duration)
        runCurrent()
    }
}
