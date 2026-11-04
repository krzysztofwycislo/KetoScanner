package pl.handsome.club.core.common

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OutcomeFlowTest {

    @Test
    fun `when upstream emits successes then their values pass through`() = runTest {
        val values = flowOf(Outcome.Success(1), Outcome.Success(2))
            .retryOnFailure { error("not expected") }
            .toList()

        assertEquals(listOf(1, 2), values)
    }

    @Test
    fun `when upstream emits a failure then onFailure gets the error and upstream is collected again`() = runTest {
        var collections = 0
        val errors = mutableListOf<AppError>()
        val upstream = flow {
            collections++
            if (collections == 1) emit(Outcome.Failure(AppError.Storage)) else emit(Outcome.Success(collections))
        }

        val values = upstream.retryOnFailure { errors += it }.take(1).toList()

        assertEquals(listOf(2), values)
        assertEquals(2, collections)
        assertEquals(listOf<AppError>(AppError.Storage), errors)
    }

    @Test
    fun `when upstream throws another exception then it propagates and onFailure is not called`() = runTest {
        var failures = 0
        val upstream = flow<Outcome<Int>> { throw IllegalStateException("boom") }

        assertFailsWith<IllegalStateException> {
            upstream.retryOnFailure { failures++ }.toList()
        }
        assertEquals(0, failures)
    }

    @Test
    fun `when the collector is cancelled then upstream is not collected again`() = runTest {
        var collections = 0
        var failures = 0
        val upstream = flow<Outcome<Int>> {
            collections++
            emit(Outcome.Success(collections))
            awaitCancellation()
        }

        val job = launch { upstream.retryOnFailure { failures++ }.collect {} }
        runCurrent()
        job.cancel()
        runCurrent()

        assertTrue(job.isCancelled)
        assertEquals(1, collections)
        assertEquals(0, failures)
    }
}
