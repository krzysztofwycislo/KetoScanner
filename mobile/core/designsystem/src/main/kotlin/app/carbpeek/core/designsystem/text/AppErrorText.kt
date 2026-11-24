package app.carbpeek.core.designsystem.text

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.R

fun AppError.toUiText(): UiText = when (this) {
    AppError.Network -> UiText.Res(R.string.designsystem_error_network)
    AppError.Timeout -> UiText.Res(R.string.designsystem_error_timeout)
    is AppError.Http -> UiText.Res(R.string.designsystem_error_server)
    AppError.NotFound -> UiText.Res(R.string.designsystem_error_not_found)
    AppError.Parsing -> UiText.Res(R.string.designsystem_error_parsing)
    AppError.Storage -> UiText.Res(R.string.designsystem_error_storage)
    is AppError.Unknown -> UiText.Res(R.string.designsystem_error_unknown)
}
