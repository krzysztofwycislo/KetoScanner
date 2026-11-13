package pl.morning.tower.karbo.feature.report.ui.state

import androidx.compose.runtime.Immutable
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.report.api.model.IssueReason
import pl.morning.tower.karbo.feature.report.api.model.IssueReport

@Immutable
internal data class IssueReportStatePrototype(
    val barcode: Barcode,
    val analysis: ProductAnalysis? = null,
    val dietMode: DietMode = DietMode.KETO,
    val reason: IssueReason? = null,
    val details: String = "",
    val expectedRate: DietRate? = null,
    val status: ReportStatus = ReportStatus.EDITING,
) {

    fun toReport(): IssueReport? {
        val reason = reason ?: return null
        return IssueReport(
            barcode = barcode,
            reason = reason,
            details = details.trim(),
            expectedRate = expectedRate.takeIf { reason == IssueReason.RATING },
            dietMode = dietMode,
        )
    }
}

internal enum class ReportStatus {
    EDITING,
    SENDING,
    SENT,
    FAILED,
}
