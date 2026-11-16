package pl.morning.tower.karbo.feature.report.ui.state

import androidx.compose.runtime.Immutable
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.report.api.model.MissingProductReport
import pl.morning.tower.karbo.feature.report.api.model.ReportPhoto
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoCaptureRequest
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoSubject

internal enum class PhotoSlot(val subject: PhotoSubject, val isRequired: Boolean) {
    FRONT(subject = PhotoSubject.FRONT, isRequired = true),
    NUTRITION_TABLE(subject = PhotoSubject.NUTRITION_TABLE, isRequired = true),
    INGREDIENTS(subject = PhotoSubject.INGREDIENTS, isRequired = false),
}

@Immutable
internal data class MissingProductStatePrototype(
    val barcode: Barcode,
    val slotPhotos: Map<PhotoSlot, ReportPhoto> = emptyMap(),
    val captureSlot: PhotoSlot = PhotoSlot.FRONT,
    val status: ReportStatus = ReportStatus.EDITING,
) {

    val photos: List<ReportPhoto> get() = PhotoSlot.entries.mapNotNull(slotPhotos::get)

    val canSend: Boolean get() = PhotoSlot.entries.filter { it.isRequired }.all { it in slotPhotos }

    fun captureRequest(): PhotoCaptureRequest = PhotoCaptureRequest(
        number = captureSlot.ordinal + 1,
        total = PhotoSlot.entries.size,
        subject = captureSlot.subject,
        namesSubject = true,
    )

    fun toReport(): MissingProductReport? {
        val front = slotPhotos[PhotoSlot.FRONT] ?: return null
        val nutritionTable = slotPhotos[PhotoSlot.NUTRITION_TABLE] ?: return null
        return MissingProductReport(
            barcode = barcode,
            front = front,
            nutritionTable = nutritionTable,
            ingredients = slotPhotos[PhotoSlot.INGREDIENTS],
        )
    }
}
