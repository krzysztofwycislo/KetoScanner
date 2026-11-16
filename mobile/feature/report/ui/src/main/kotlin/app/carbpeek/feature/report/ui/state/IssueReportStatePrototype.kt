package app.carbpeek.feature.report.ui.state

import androidx.compose.runtime.Immutable
import app.carbpeek.core.model.Barcode
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.report.api.model.IssueReason
import app.carbpeek.feature.report.api.model.IssueReport
import app.carbpeek.feature.report.api.model.ReportPhoto
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureRequest
import app.carbpeek.feature.report.ui.capture.state.PhotoSubject

@Immutable
internal data class IssueReportStatePrototype(
    val barcode: Barcode,
    val analysis: ProductAnalysis? = null,
    val dietMode: DietMode = DietMode.KETO,
    val reason: IssueReason? = null,
    val details: String = "",
    val expectedRate: DietRate? = null,
    val status: ReportStatus = ReportStatus.EDITING,
    val photos: List<ReportPhoto> = emptyList(),
) {

    val showsPhotos: Boolean get() = reason != IssueReason.RATING

    val canAddPhoto: Boolean get() = showsPhotos && photos.size < MAX_REPORT_PHOTOS

    fun captureRequest(): PhotoCaptureRequest = PhotoCaptureRequest(
        number = photos.size + 1,
        total = MAX_REPORT_PHOTOS,
        subject = if (reason == IssueReason.NUTRITION) PhotoSubject.NUTRITION_TABLE else PhotoSubject.LABEL,
    )

    fun toReport(): IssueReport? {
        val reason = reason ?: return null
        return IssueReport(
            barcode = barcode,
            reason = reason,
            details = details.trim(),
            expectedRate = expectedRate.takeIf { reason == IssueReason.RATING },
            dietMode = dietMode,
            photos = photos.takeIf { reason != IssueReason.RATING } ?: emptyList(),
        )
    }
}

internal const val MAX_REPORT_PHOTOS = 3
