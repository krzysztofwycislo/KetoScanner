package pl.morning.tower.karbo.core.designsystem.text

import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.designsystem.R

fun AppError.toUiText(): UiText = when (this) {
    AppError.Network -> UiText.Res(R.string.designsystem_error_network)
    is AppError.Http -> UiText.Res(R.string.designsystem_error_server)
    AppError.NotFound -> UiText.Res(R.string.designsystem_error_not_found)
    AppError.Parsing -> UiText.Res(R.string.designsystem_error_parsing)
    AppError.Storage -> UiText.Res(R.string.designsystem_error_storage)
    is AppError.Unknown -> UiText.Res(R.string.designsystem_error_unknown)
}
