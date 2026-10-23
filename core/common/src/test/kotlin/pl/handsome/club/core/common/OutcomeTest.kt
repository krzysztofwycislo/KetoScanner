package pl.handsome.club.core.common

import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class OutcomeTest {

    @Test
    fun `when map on Success then value is transformed`() {
        val result = Outcome.Success(2).map { it * 10 }

        assertEquals(Outcome.Success(20), result)
    }

    @Test
    fun `when map on Failure then failure is unchanged`() {
        val failure: Outcome<Int> = FAILURE

        val result = failure.map { it * 10 }

        assertSame(FAILURE, result)
    }

    @Test
    fun `when fold on Success then onSuccess is called`() {
        val result = Outcome.Success(3).fold(onSuccess = { "value $it" }, onFailure = { "error $it" })

        assertEquals("value 3", result)
    }

    @Test
    fun `when fold on Failure then onFailure is called`() {
        val failure: Outcome<Int> = FAILURE

        val result = failure.fold(onSuccess = { "value $it" }, onFailure = { "error $it" })

        assertEquals("error ${AppError.Network}", result)
    }

    @Test
    fun `when getOrNull on Success then value is returned`() {
        assertEquals("x", Outcome.Success("x").getOrNull())
    }

    @Test
    fun `when getOrNull on Failure then null is returned`() {
        val failure: Outcome<String> = FAILURE

        assertNull(failure.getOrNull())
    }

    @Test
    fun `when outcomeOf block returns then Success is returned`() {
        val result = outcomeOf(mapError = { AppError.Unknown(it) }) { 42 }

        assertEquals(Outcome.Success(42), result)
    }

    @Test
    fun `when outcomeOf block throws then error is mapped`() {
        val result = outcomeOf<Int>(mapError = { AppError.Storage }) { throw IllegalStateException("boom") }

        assertEquals(Outcome.Failure(AppError.Storage), result)
    }

    @Test
    fun `when outcomeOf block throws CancellationException then it is rethrown`() {
        assertFailsWith<CancellationException> {
            outcomeOf<Int>(mapError = { AppError.Unknown(it) }) { throw CancellationException("cancelled") }
        }
    }

    private companion object {
        val FAILURE = Outcome.Failure(AppError.Network)
    }
}
