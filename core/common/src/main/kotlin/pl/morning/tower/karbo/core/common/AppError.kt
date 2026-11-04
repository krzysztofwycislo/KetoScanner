package pl.morning.tower.karbo.core.common

sealed interface AppError {

    data object Network : AppError

    data class Http(val code: Int) : AppError

    data object NotFound : AppError

    data object Parsing : AppError

    data object Storage : AppError

    data class Unknown(val cause: Throwable) : AppError
}

val AppError.isReportable: Boolean
    get() = when (this) {
        AppError.Network -> false
        AppError.NotFound -> false
        is AppError.Http -> true
        AppError.Parsing -> true
        AppError.Storage -> true
        is AppError.Unknown -> true
    }
