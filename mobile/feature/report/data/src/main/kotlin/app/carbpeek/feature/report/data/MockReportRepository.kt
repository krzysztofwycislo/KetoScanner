package app.carbpeek.feature.report.data

import app.carbpeek.core.common.AppLogger
import app.carbpeek.core.common.Outcome
import app.carbpeek.feature.report.api.ReportRepository
import app.carbpeek.feature.report.api.model.IssueReport
import app.carbpeek.feature.report.api.model.MissingProductReport
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

internal class MockReportRepository(private val logger: AppLogger) : ReportRepository {

    override suspend fun submitIssue(report: IssueReport): Outcome<Unit> {
        delay(1.seconds)
        logger.warn(TAG, "Mock backend, report not sent: $report")
        return Outcome.Success(Unit)
    }

    override suspend fun submitMissingProduct(report: MissingProductReport): Outcome<Unit> {
        delay(1.seconds)
        logger.warn(TAG, "Mock backend, missing product not sent: $report")
        return Outcome.Success(Unit)
    }

    private companion object {
        const val TAG = "MockReportRepository"
    }
}
