package pl.handsome.club.core.common

interface ErrorReporter {
    fun report(tag: String, message: String, error: AppError)
}

class LoggingErrorReporter(private val logger: AppLogger) : ErrorReporter {

    override fun report(tag: String, message: String, error: AppError) {
        logger.error(tag, "$message: $error", (error as? AppError.Unknown)?.cause)
    }
}
