package pl.morning.tower.karbo.feature.report.ui.capture

import pl.morning.tower.karbo.core.designsystem.text.UiArg
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.report.ui.R
import pl.morning.tower.karbo.feature.report.ui.capture.state.CameraPermission
import pl.morning.tower.karbo.feature.report.ui.capture.state.PhotoSubject

internal class PhotoCaptureResources {

    fun counter(number: Int, total: Int): UiText = UiText.Res(
        R.string.report_photo_counter,
        UiArg.Numeric.Count(number.toLong()),
        UiArg.Numeric.Count(total.toLong()),
    )

    fun frameHint(subject: PhotoSubject): UiText = UiText.Res(
        when (subject) {
            PhotoSubject.LABEL -> R.string.report_capture_frame_label
            PhotoSubject.NUTRITION_TABLE -> R.string.report_capture_frame_nutrition
        },
    )

    fun previewTitle(subject: PhotoSubject): UiText = UiText.Res(
        when (subject) {
            PhotoSubject.LABEL -> R.string.report_preview_label_title
            PhotoSubject.NUTRITION_TABLE -> R.string.report_preview_nutrition_title
        },
    )

    fun previewBody(subject: PhotoSubject): UiText = UiText.Res(
        when (subject) {
            PhotoSubject.LABEL -> R.string.report_preview_label_body
            PhotoSubject.NUTRITION_TABLE -> R.string.report_preview_nutrition_body
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
}
