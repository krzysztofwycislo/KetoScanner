package pl.handsome.club.core.common

import kotlin.coroutines.cancellation.CancellationException

/** Result of an operation that can fail with an [AppError]. */
sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>
    data class Failure(val error: AppError) : Outcome<Nothing>
}

inline fun <T, R> Outcome<T>.map(transform: (T) -> R): Outcome<R> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}

inline fun <T, R> Outcome<T>.fold(onSuccess: (T) -> R, onFailure: (AppError) -> R): R = when (this) {
    is Outcome.Success -> onSuccess(value)
    is Outcome.Failure -> onFailure(error)
}

fun <T> Outcome<T>.getOrNull(): T? = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> null
}

/**
 * Runs [block] and wraps its result; anything thrown becomes [Outcome.Failure] through [mapError].
 * This is the single place the cancellation rule from architecture section 8 is implemented:
 * [CancellationException] is always rethrown, so coroutine cancellation is never swallowed.
 */
inline fun <T> outcomeOf(mapError: (Throwable) -> AppError, block: () -> T): Outcome<T> =
    try {
        Outcome.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Outcome.Failure(mapError(e))
    }
