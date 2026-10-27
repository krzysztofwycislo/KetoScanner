package pl.handsome.club.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
@ReadOnlyComposable
fun Rating.color(): Color {
    val colors = AppTheme.colors.result
    return when (this) {
        Rating.Positive -> colors.positive
        Rating.Neutral -> colors.neutral
        Rating.Negative -> colors.negative
        Rating.Unknown -> colors.negative
    }
}
