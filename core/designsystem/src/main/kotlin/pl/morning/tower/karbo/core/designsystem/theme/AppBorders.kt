package pl.handsome.club.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppBorders(
    val hairline: Dp,
    val control: Dp,
    val selected: Dp,
    val scanButtonRing: Dp,
)

val DefaultAppBorders = AppBorders(
    hairline = 1.dp,
    control = 1.5.dp,
    selected = 2.dp,
    scanButtonRing = 5.dp,
)
