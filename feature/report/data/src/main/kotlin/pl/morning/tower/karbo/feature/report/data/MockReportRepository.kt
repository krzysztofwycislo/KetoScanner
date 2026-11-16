package pl.morning.tower.karbo.feature.report.data

import kotlinx.coroutines.delay
import pl.morning.tower.karbo.core.common.AppLogger
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.report.api.ReportRepository
import pl.morning.tower.karbo.feature.report.api.model.IssueReport
import pl.morning.tower.karbo.feature.report.api.model.MissingProductReport
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
