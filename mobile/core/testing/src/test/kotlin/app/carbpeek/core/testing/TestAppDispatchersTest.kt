package app.carbpeek.core.testing

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestAppDispatchersTest {

    @Test
    fun `when work is launched on io then it shares the runTest scheduler`() = runTest {
        val dispatchers = TestAppDispatchers(testScheduler)
        var done = false

        launch(dispatchers.io) {
            delay(DELAY_MS)
            done = true
        }
        assertFalse(done)
        advanceUntilIdle()

        assertTrue(done)
        assertEquals(DELAY_MS, currentTime)
    }

    @Test
    fun `when created then io and default are the same dispatcher`() {
        val dispatchers = TestAppDispatchers()

        assertSame(dispatchers.io, dispatchers.default)
    }

    private companion object {
        const val DELAY_MS = 1_000L
    }
}
