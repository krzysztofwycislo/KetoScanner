package app.carbpeek.openfoodapi

import app.carbpeek.core.common.AppError
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

internal fun Throwable.toAppError(): AppError = when (this) {
    is ContentConvertException -> AppError.Parsing
    is SerializationException -> AppError.Parsing
    is NoTransformationFoundException -> AppError.Parsing
    is IOException if isTimeout() -> AppError.Timeout
    is IOException -> AppError.Network
    is ResponseException -> AppError.Http(response.status.value)
    else -> AppError.Unknown(this)
}

private fun Throwable.isTimeout(): Boolean =
    generateSequence(this) { it.cause }.any { error ->
        error is HttpRequestTimeoutException ||
            error is ConnectTimeoutException ||
            error is SocketTimeoutException
    }
