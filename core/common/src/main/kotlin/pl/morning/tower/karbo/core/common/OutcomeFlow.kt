package pl.handsome.club.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen

fun <T> Flow<Outcome<T>>.retryOnFailure(onFailure: suspend (AppError) -> Unit): Flow<T> =
    map { outcome ->
        when (outcome) {
            is Outcome.Success -> outcome.value
            is Outcome.Failure -> throw OutcomeFailure(outcome.error)
        }
    }.retryWhen { cause, _ ->
        if (cause is OutcomeFailure) {
            onFailure(cause.error)
            true
        } else {
            false
        }
    }

private class OutcomeFailure(val error: AppError) : Exception()
