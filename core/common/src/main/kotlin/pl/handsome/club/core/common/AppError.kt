package pl.handsome.club.core.common

/**
 * Technical failure reported by `data` code. Domain rule outcomes (for example a product without nutrition data)
 * are modelled by each feature's own types, not here.
 */
sealed interface AppError {

    /** No connection, DNS failure or timeout: the request never got an HTTP response. */
    data object Network : AppError

    /** The server answered with a non-success HTTP status that is not covered by [NotFound]. */
    data class Http(val code: Int) : AppError

    /** The requested resource does not exist (HTTP 404 or an API "not found" status). */
    data object NotFound : AppError

    /** A response arrived but could not be parsed or mapped into a model. */
    data object Parsing : AppError

    /** Reading or writing local storage (database or DataStore) failed. */
    data object Storage : AppError

    /** Anything not covered above. The cause is kept for logging. */
    data class Unknown(val cause: Throwable) : AppError
}
