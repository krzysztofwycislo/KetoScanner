package app.carbpeek.feature.report.api

import app.carbpeek.core.common.Outcome
import app.carbpeek.feature.report.api.model.IssueReport
import app.carbpeek.feature.report.api.model.MissingProductReport

interface ReportRepository {
    suspend fun submitIssue(report: IssueReport): Outcome<Unit>

    suspend fun submitMissingProduct(report: MissingProductReport): Outcome<Unit>
}
