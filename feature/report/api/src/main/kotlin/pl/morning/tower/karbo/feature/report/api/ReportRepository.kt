package pl.morning.tower.karbo.feature.report.api

import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.report.api.model.IssueReport

interface ReportRepository {
    suspend fun submitIssue(report: IssueReport): Outcome<Unit>
}
