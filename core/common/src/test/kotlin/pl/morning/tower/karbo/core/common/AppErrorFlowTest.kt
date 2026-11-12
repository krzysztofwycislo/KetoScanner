package pl.morning.tower.karbo.core.common

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppErrorFlowTest {

    @Test
    fun `when upstream emits values then retryOnAppError passes them through`() = runTest {
        val values = flowOf(1, 2)
            .retryOnAppError { error("not expected") }
            .toList()

        assertEquals(listOf(1, 2), values)
    }

    @Test
    fun `when upstream throws an AppErrorException then onError gets the error and upstream is collected again`() =
        runTest {
            var collections = 0
            val errors = mutableListOf<AppError>()
            val upstream = flow {
                collections++
                if (collections == 1) {
                    throw AppErrorException(AppError.Storage)
                }
                emit(collections)
            }

            val values = upstream.retryOnAppError { errors += it }.take(1).toList()

            assertEquals(listOf(2), values)
            assertEquals(2, collections)
            assertEquals(listOf<AppError>(AppError.Storage), errors)
        }

    @Test
    fun `when upstream throws another exception then retryOnAppError propagates it without onError`() = runTest {
        var failures = 0
        val upstream = flow<Int> { throw IllegalStateException("boom") }

        assertFailsWith<IllegalStateException> {
            upstream.retryOnAppError { failures++ }.toList()
        }
        assertEquals(0, failures)
    }

    @Test
    fun `when the collector is cancelled then retryOnAppError does not collect upstream again`() = runTest {
        var collections = 0
        var failures = 0
        val upstream = flow {
            collections++
            emit(collections)
            awaitCancellation()
        }

        val job = launch { upstream.retryOnAppError { failures++ }.collect {} }
        runCurrent()
        job.cancel()
        runCurrent()

        assertTrue(job.isCancelled)
        assertEquals(1, collections)
        assertEquals(0, failures)
    }

    @Test
    fun `when upstream emits values then mapFailures passes them through`() = runTest {
        val values = flowOf(1, 2)
            .mapFailures { error("not expected") }
            .toList()

        assertEquals(listOf(1, 2), values)
    }

    @Test
    fun `when upstream throws an untyped exception then mapFailures throws the mapped error`() = runTest {
        val mapped = mutableListOf<Throwable>()
        val upstream = flow<Int> { throw IllegalStateException("boom") }

        val exception = assertFailsWith<AppErrorException> {
            upstream.mapFailures { cause ->
                mapped += cause
                AppError.Storage
            }.toList()
        }

        assertEquals(AppError.Storage, exception.error)
        assertEquals(1, mapped.size)
        assertTrue(mapped.single() is IllegalStateException)
    }

    @Test
    fun `when upstream throws an AppErrorException then mapFailures passes it without calling mapError`() = runTest {
        var mapCalls = 0
        val upstream = flow<Int> { throw AppErrorException(AppError.Parsing) }

        val exception = assertFailsWith<AppErrorException> {
            upstream.mapFailures {
                mapCalls++
                AppError.Storage
            }.toList()
        }

        assertEquals(AppError.Parsing, exception.error)
        assertEquals(0, mapCalls)
    }

    @Test
    fun `when upstream throws CancellationException then mapFailures rethrows it`() = runTest {
        var mapCalls = 0
        val upstream = flow<Int> { throw CancellationException("cancelled") }

        assertFailsWith<CancellationException> {
            upstream.mapFailures {
                mapCalls++
                AppError.Storage
            }.toList()
        }
        assertEquals(0, mapCalls)
    }

    @Test
    fun `when upstream throws an AppErrorException then catchAppError gets the error and completes`() = runTest {
        val errors = mutableListOf<AppError>()
        val upstream = flow {
            emit(1)
            throw AppErrorException(AppError.Storage)
        }

        val values = upstream.catchAppError { errors += it }.toList()

        assertEquals(listOf(1), values)
        assertEquals(listOf<AppError>(AppError.Storage), errors)
    }

    @Test
    fun `when upstream throws another exception then catchAppError rethrows it`() = runTest {
        var errors = 0
        val upstream = flow<Int> { throw IllegalStateException("boom") }

        assertFailsWith<IllegalStateException> {
            upstream.catchAppError { errors++ }.toList()
        }
        assertEquals(0, errors)
    }

    @Test
    fun `when upstream throws CancellationException then catchAppError does not catch it`() = runTest {
        var errors = 0
        val upstream = flow<Int> { throw CancellationException("cancelled") }

        assertFailsWith<CancellationException> {
            upstream.catchAppError { errors++ }.toList()
        }
        assertEquals(0, errors)
    }
}
