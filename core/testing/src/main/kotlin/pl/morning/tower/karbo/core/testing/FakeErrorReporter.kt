package pl.morning.tower.karbo.core.testing

import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.ErrorReporter

class FakeErrorReporter : ErrorReporter {

    data class Report(val tag: String, val message: String, val error: AppError)

    val reports = mutableListOf<Report>()

    override fun report(tag: String, message: String, error: AppError) {
        reports += Report(tag, message, error)
    }
}
