package pl.handsome.club.feature.scanner.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText

internal class ScanResources {

    fun rationaleTitle(): UiText = UiText.Res(R.string.scan_camera_permission_rationale_title)

    fun deniedTitle(): UiText = UiText.Res(R.string.scan_camera_permission_denied_title)

    fun permissionRationale(): UiText = UiText.Res(R.string.scan_camera_permission_rationale)

    fun allowCamera(): UiText = UiText.Res(R.string.scan_camera_permission_allow)

    fun permissionDenied(): UiText = UiText.Res(R.string.scan_camera_permission_denied)

    fun openSettings(): UiText = UiText.Res(R.string.scan_camera_permission_open_settings)

    fun searchFailed(error: AppError): UiText =
        if (error == AppError.NotFound) UiText.Res(R.string.scan_product_not_found) else error.toUiText()
}
