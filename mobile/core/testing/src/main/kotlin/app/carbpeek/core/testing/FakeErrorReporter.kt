package app.carbpeek.core.testing

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.ErrorReporter

class FakeErrorReporter : ErrorReporter {

    data class Report(val tag: String, val message: String, val error: AppError)

    val reports = mutableListOf<Report>()

    override fun report(tag: String, message: String, error: AppError) {
        reports += Report(tag, message, error)
    }
}
