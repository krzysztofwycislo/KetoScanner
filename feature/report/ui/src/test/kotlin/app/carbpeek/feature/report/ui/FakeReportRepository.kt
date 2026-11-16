package app.carbpeek.feature.report.ui

import app.carbpeek.core.common.Outcome
import app.carbpeek.feature.report.api.ReportRepository
import app.carbpeek.feature.report.api.model.IssueReport
import app.carbpeek.feature.report.api.model.MissingProductReport
import kotlinx.coroutines.CompletableDeferred

internal class FakeReportRepository : ReportRepository {

    var result: Outcome<Unit> = Outcome.Success(Unit)
    val submittedIssues = mutableListOf<IssueReport>()
    val submittedMissingProducts = mutableListOf<MissingProductReport>()
    private var gate: CompletableDeferred<Unit>? = null

    fun gate(): CompletableDeferred<Unit> = CompletableDeferred<Unit>().also { gate = it }

    override suspend fun submitIssue(report: IssueReport): Outcome<Unit> {
        submittedIssues += report
        gate?.await()
        return result
    }

    override suspend fun submitMissingProduct(report: MissingProductReport): Outcome<Unit> {
        submittedMissingProducts += report
        gate?.await()
        return result
    }
}
