package app.carbpeek.feature.report.api.model

import app.carbpeek.core.model.Barcode
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.preferences.api.model.DietMode

data class IssueReport(
    val barcode: Barcode,
    val reason: IssueReason,
    val details: String,
    val expectedRate: DietRate?,
    val dietMode: DietMode?,
    val photos: List<ReportPhoto>,
)
