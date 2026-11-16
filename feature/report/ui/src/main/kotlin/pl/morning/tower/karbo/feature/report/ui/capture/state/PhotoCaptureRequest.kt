package pl.morning.tower.karbo.feature.report.ui.capture.state

internal data class PhotoCaptureRequest(
    val number: Int,
    val total: Int,
    val subject: PhotoSubject,
)

internal enum class PhotoSubject {
    LABEL,
    NUTRITION_TABLE,
}
