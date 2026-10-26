package pl.handsome.club.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppBorders(
    val hairline: Dp,
)

val DefaultAppBorders = AppBorders(
    hairline = 1.dp,
)
