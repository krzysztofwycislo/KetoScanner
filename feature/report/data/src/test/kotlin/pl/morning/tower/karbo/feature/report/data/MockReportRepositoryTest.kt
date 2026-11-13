package pl.morning.tower.karbo.feature.report.data

import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.core.testing.FakeAppLogger
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.report.api.model.IssueReason
import pl.morning.tower.karbo.feature.report.api.model.IssueReport
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

    private companion object {
        const val BARCODE = "5449000000996"

        val report = IssueReport(
            barcode = Barcode(BARCODE),
            reason = IssueReason.RATING,
            details = "Plain milk should be fine",
            expectedRate = DietRate.NEUTRAL,
            dietMode = DietMode.KETO,
        )
    }
}
