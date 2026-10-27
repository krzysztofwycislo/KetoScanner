package pl.handsome.club.openfoodapi

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import pl.handsome.club.core.common.AppError
import retrofit2.HttpException
import java.io.IOException

internal fun Throwable.toAppError(): AppError = when (this) {
    is JsonDataException -> AppError.Parsing
    is JsonEncodingException -> AppError.Parsing
    is IOException -> AppError.Network
    is HttpException -> AppError.Http(code())
    else -> AppError.Unknown(this)
}
