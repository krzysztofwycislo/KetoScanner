package app.carbpeek.feature.report.ui.capture.state

internal data class PhotoCaptureRequest(
    val number: Int,
    val total: Int,
    val subject: PhotoSubject,
    val namesSubject: Boolean = false,
)

internal enum class PhotoSubject {
    LABEL,
    FRONT,
    NUTRITION_TABLE,
    INGREDIENTS,
}
