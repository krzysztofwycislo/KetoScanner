package app.carbpeek.feature.report.ui.capture

import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.report.ui.R
import app.carbpeek.feature.report.ui.capture.state.CameraPermission
import app.carbpeek.feature.report.ui.capture.state.PhotoCaptureRequest
import app.carbpeek.feature.report.ui.capture.state.PhotoSubject

internal class PhotoCaptureResources {

    fun counter(request: PhotoCaptureRequest): UiText = if (request.namesSubject) {
        UiText.Res(
            R.string.report_photo_counter_named,
            UiArg.Numeric.Count(request.number.toLong()),
            UiArg.Numeric.Count(request.total.toLong()),
            UiArg.Nested(subjectName(request.subject)),
        )
    } else {
        UiText.Res(
            R.string.report_photo_counter,
            UiArg.Numeric.Count(request.number.toLong()),
            UiArg.Numeric.Count(request.total.toLong()),
        )
    }

    fun frameHint(subject: PhotoSubject): UiText = UiText.Res(
        when (subject) {
            PhotoSubject.LABEL -> R.string.report_capture_frame_label
            PhotoSubject.FRONT -> R.string.report_capture_frame_front
            PhotoSubject.NUTRITION_TABLE -> R.string.report_capture_frame_nutrition
            PhotoSubject.INGREDIENTS -> R.string.report_capture_frame_ingredients
        },
    )

    fun previewTitle(subject: PhotoSubject): UiText = UiText.Res(
        when (subject) {
            PhotoSubject.LABEL -> R.string.report_preview_label_title
            PhotoSubject.FRONT -> R.string.report_preview_front_title
            PhotoSubject.NUTRITION_TABLE -> R.string.report_preview_nutrition_title
            PhotoSubject.INGREDIENTS -> R.string.report_preview_ingredients_title
        },
    )

    fun previewBody(subject: PhotoSubject): UiText = UiText.Res(
        when (subject) {
            PhotoSubject.LABEL -> R.string.report_preview_label_body
            PhotoSubject.FRONT -> R.string.report_preview_front_body
            PhotoSubject.NUTRITION_TABLE -> R.string.report_preview_nutrition_body
            PhotoSubject.INGREDIENTS -> R.string.report_preview_ingredients_body
        },
    )

    fun torchLabel(isOn: Boolean): UiText =
        UiText.Res(if (isOn) R.string.report_torch_off else R.string.report_torch_on)

    fun blockedTitle(permission: CameraPermission): UiText = UiText.Res(
        when (permission) {
            CameraPermission.Rationale -> R.string.report_camera_rationale_title
            else -> R.string.report_camera_blocked_title
        },
    )

    fun blockedBody(permission: CameraPermission): UiText = UiText.Res(
        when (permission) {
            CameraPermission.Rationale -> R.string.report_camera_rationale_body
            else -> R.string.report_camera_blocked_body
        },
    )

    fun blockedButton(permission: CameraPermission): UiText = UiText.Res(
        when (permission) {
            CameraPermission.Rationale -> R.string.report_allow_camera
            else -> R.string.report_open_settings
        },
    )

    fun photoImportFailed(): UiText = UiText.Res(R.string.report_photo_import_failed)

    fun photoCaptureFailed(): UiText = UiText.Res(R.string.report_photo_capture_failed)

    private fun subjectName(subject: PhotoSubject): UiText = UiText.Res(
        when (subject) {
            PhotoSubject.LABEL -> R.string.report_subject_label
            PhotoSubject.FRONT -> R.string.report_slot_front
            PhotoSubject.NUTRITION_TABLE -> R.string.report_slot_nutrition
            PhotoSubject.INGREDIENTS -> R.string.report_slot_ingredients
        },
    )
}
