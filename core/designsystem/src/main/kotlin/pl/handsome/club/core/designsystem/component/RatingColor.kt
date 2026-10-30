package pl.handsome.club.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.core.designsystem.theme.toneFor

@Composable
@ReadOnlyComposable
fun Rating.color(): Color = AppTheme.colors.verdict.toneFor(this).content

@Composable
@ReadOnlyComposable
fun Rating.containerColor(): Color = AppTheme.colors.verdict.toneFor(this).container

@Composable
@ReadOnlyComposable
fun Rating.cameraColor(): Color {
    val camera = AppTheme.colors.camera
    return when (this) {
        Rating.Positive -> camera.ratingPositive
        Rating.Neutral -> camera.ratingNeutral
        Rating.Negative -> camera.ratingNegative
        Rating.Unknown -> camera.ratingUnknown
    }
}
