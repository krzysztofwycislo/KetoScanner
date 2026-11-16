package pl.morning.tower.karbo.feature.report.ui

import kotlinx.coroutines.CompletableDeferred
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.feature.report.api.ReportRepository
import pl.morning.tower.karbo.feature.report.api.model.IssueReport
import pl.morning.tower.karbo.feature.report.api.model.MissingProductReport

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
