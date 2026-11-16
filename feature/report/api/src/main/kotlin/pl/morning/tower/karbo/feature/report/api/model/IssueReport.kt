package pl.morning.tower.karbo.feature.report.api.model

import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode

data class IssueReport(
    val barcode: Barcode,
    val reason: IssueReason,
    val details: String,
    val expectedRate: DietRate?,
    val dietMode: DietMode,
    val photos: List<ReportPhoto>,
)
