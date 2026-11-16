package pl.morning.tower.karbo.feature.report.ui.state

import androidx.compose.runtime.Immutable
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.report.api.model.IssueReason
import pl.morning.tower.karbo.feature.report.api.model.IssueReport
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCaptureRequest
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoSubject

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

internal enum class ReportStatus {
    EDITING,
    SENDING,
    SENT,
    FAILED,
}
