package app.carbpeek.feature.report.data

import app.carbpeek.core.common.Outcome
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.testing.FakeAppLogger
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.report.api.model.IssueReason
import app.carbpeek.feature.report.api.model.IssueReport
import app.carbpeek.feature.report.api.model.MissingProductReport
import app.carbpeek.feature.report.api.model.ReportPhoto
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockReportRepositoryTest {

    private val logger = FakeAppLogger()
    private val repository = MockReportRepository(logger)

    @Test
    fun `when an issue is submitted then it succeeds after a second and logs one warning with the barcode`() =
        runTest {
            val outcome = repository.submitIssue(report)

            assertEquals(Outcome.Success(Unit), outcome)
            assertEquals(1_000L, currentTime)
            val entry = logger.entries.single()
            assertEquals(FakeAppLogger.Level.Warn, entry.level)
            assertTrue(entry.message.contains(BARCODE))
        }

    @Test
    fun `when a missing product is submitted then it succeeds after a second and logs one warning with the barcode`() =
        runTest {
            val outcome = repository.submitMissingProduct(missingProduct)

            assertEquals(Outcome.Success(Unit), outcome)
            assertEquals(1_000L, currentTime)
            val entry = logger.entries.single()
            assertEquals(FakeAppLogger.Level.Warn, entry.level)
            assertTrue(entry.message.contains(BARCODE))
        }

    private companion object {
        const val BARCODE = "5449000000996"

        val report = IssueReport(
            barcode = Barcode(BARCODE),
            reason = IssueReason.RATING,
            details = "Plain milk should be fine",
            expectedRate = DietRate.NEUTRAL,
            dietMode = DietMode.KETO,
            photos = listOf(ReportPhoto("file:///data/report-photos/1")),
        )

        val missingProduct = MissingProductReport(
            barcode = Barcode(BARCODE),
            front = ReportPhoto("file:///data/report-photos/1"),
            nutritionTable = ReportPhoto("file:///data/report-photos/2"),
            ingredients = null,
        )
    }
}
