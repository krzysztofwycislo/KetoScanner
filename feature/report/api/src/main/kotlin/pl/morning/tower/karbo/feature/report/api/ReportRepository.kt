package pl.morning.tower.karbo.feature.report.api

import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.report.api.model.IssueReport
import pl.morning.tower.karbo.feature.report.api.model.MissingProductReport

interface ReportRepository {
    suspend fun submitIssue(report: IssueReport): Outcome<Unit>

    suspend fun submitMissingProduct(report: MissingProductReport): Outcome<Unit>
}
