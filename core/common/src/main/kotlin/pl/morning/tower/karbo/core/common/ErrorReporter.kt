package pl.morning.tower.karbo.core.common

interface ErrorReporter {
    fun report(tag: String, message: String, error: AppError)
}

class LoggingErrorReporter(private val logger: AppLogger) : ErrorReporter {

    override fun report(tag: String, message: String, error: AppError) {
        if (error.isReportable) {
            logger.error(tag, "$message: $error", (error as? AppError.Unknown)?.cause)
        } else {
            logger.warn(tag, "$message: $error")
        }
    }
}
