package app.carbpeek.server.product

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class RequestBudgetTest {

    private val timeSource = TestTimeSource()

    @Test
    fun `when the limit is used up then the next request in the window is refused`() {
        val budget = RequestBudget(limit = 3, timeSource = timeSource)

        repeat(3) { assertNull(budget.tryAcquire()) }

        assertNotNull(budget.tryAcquire())
    }

    @Test
    fun `when the window slides then each request is given back 60 seconds after it was made`() {
        val budget = RequestBudget(limit = 2, timeSource = timeSource)
        assertNull(budget.tryAcquire())
        timeSource += 40.seconds
        assertNull(budget.tryAcquire())

        timeSource += 19.seconds
        assertNotNull(budget.tryAcquire())
        timeSource += 1.seconds
        assertNull(budget.tryAcquire())
        assertNotNull(budget.tryAcquire())
        timeSource += 40.seconds
        assertNull(budget.tryAcquire())
    }

    @Test
    fun `when a request is refused then it returns the time until a request is given back`() {
        val budget = RequestBudget(limit = 2, timeSource = timeSource)
        budget.tryAcquire()
        timeSource += 10.seconds
        budget.tryAcquire()
        timeSource += 5.seconds

        assertEquals(expected = 45.seconds, actual = budget.tryAcquire())
    }

    @Test
    fun `when a request is given back in less than a second then the wait is one second`() {
        val budget = RequestBudget(limit = 1, timeSource = timeSource)
        budget.tryAcquire()
        timeSource += 59_900.milliseconds

        assertEquals(expected = 1.seconds, actual = budget.tryAcquire())
    }

    @Test
    fun `when a refused request asks again then it does not use up the budget`() {
        val budget = RequestBudget(limit = 1, timeSource = timeSource)
        budget.tryAcquire()
        timeSource += 30.seconds
        repeat(5) { assertNotNull(budget.tryAcquire()) }
        timeSource += 30.seconds

        assertNull(budget.tryAcquire())
    }

    @Test
    fun `when a part is reserved then requests that respect it stop earlier than the others`() {
        val budget = RequestBudget(limit = 4, timeSource = timeSource)

        assertNull(budget.tryAcquire(reserved = 2))
        assertNull(budget.tryAcquire(reserved = 2))
        assertNotNull(budget.tryAcquire(reserved = 2))
        assertNull(budget.tryAcquire())
        assertNull(budget.tryAcquire())
        assertNotNull(budget.tryAcquire())
    }

    @Test
    fun `when a reserved part is refused then the wait ends when enough requests are given back`() {
        val budget = RequestBudget(limit = 4, timeSource = timeSource)
        repeat(4) {
            budget.tryAcquire()
            timeSource += 10.seconds
        }

        assertEquals(expected = 20.seconds, actual = budget.tryAcquire())
        assertEquals(expected = 40.seconds, actual = budget.tryAcquire(reserved = 2))
    }
}
