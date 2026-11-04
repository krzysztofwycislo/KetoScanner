package pl.morning.tower.karbo.openfoodapi

import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import pl.morning.tower.karbo.core.common.AppError

internal fun Throwable.toAppError(): AppError = when (this) {
    is ContentConvertException -> AppError.Parsing
    is SerializationException -> AppError.Parsing
    is NoTransformationFoundException -> AppError.Parsing
    is IOException -> AppError.Network
    is ResponseException -> AppError.Http(response.status.value)
    else -> AppError.Unknown(this)
}
