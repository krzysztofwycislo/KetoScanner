package pl.morning.tower.karbo.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import kotlin.coroutines.cancellation.CancellationException

fun <T> Flow<T>.mapFailures(mapError: (Throwable) -> AppError): Flow<T> =
    catch { cause ->
        if (cause is CancellationException || cause is AppErrorException) {
            throw cause
        }
        throw AppErrorException(mapError(cause))
    }

fun <T> Flow<T>.catchAppError(onError: suspend (AppError) -> Unit): Flow<T> =
    catch { cause ->
        if (cause is AppErrorException) {
            onError(cause.error)
        } else {
            throw cause
        }
    }

fun <T> Flow<T>.retryOnAppError(onError: suspend (AppError) -> Unit): Flow<T> =
    retryWhen { cause, _ ->
        if (cause is AppErrorException) {
            onError(cause.error)
            true
        } else {
            false
        }
    }
