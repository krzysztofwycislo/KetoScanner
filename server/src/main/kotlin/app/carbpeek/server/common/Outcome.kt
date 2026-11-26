package app.carbpeek.server.common

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration

internal sealed interface Outcome<out T> {
    data class Success<T>(
        val value: T,
    ) : Outcome<T>

    data class Failure(
        val error: ServerError,
    ) : Outcome<Nothing>
}

internal inline fun <T> outcomeOf(
    mapError: (Exception) -> ServerError,
    block: () -> T,
): Outcome<T> =
    try {
        Outcome.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Outcome.Failure(mapError(e))
    }

internal inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> =
    when (this) {
        is Outcome.Success -> Outcome.Success(transform(value))
        is Outcome.Failure -> this
    }

internal inline fun <T, R> Outcome<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (ServerError) -> R,
): R =
    when (this) {
        is Outcome.Success -> onSuccess(value)
        is Outcome.Failure -> onFailure(error)
    }

internal sealed interface ServerError {
    data object NotFound : ServerError

    data object InvalidRequest : ServerError

    data class Unavailable(
        val retryAfter: Duration,
    ) : ServerError
}
