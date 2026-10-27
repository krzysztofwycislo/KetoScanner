package pl.handsome.club.core.common

sealed interface AppError {

    data object Network : AppError

    data class Http(val code: Int) : AppError

    data object NotFound : AppError

    data object Parsing : AppError

    data object Storage : AppError

    data class Unknown(val cause: Throwable) : AppError
}
