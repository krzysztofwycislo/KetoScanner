package app.carbpeek.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSpacing(
    val xxs: Dp,
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp,
    val xxl: Dp,
)

val DefaultAppSpacing = AppSpacing(
    xxs = 4.dp,
    xs = 8.dp,
    s = 12.dp,
    m = 16.dp,
    l = 20.dp,
    xl = 32.dp,
    xxl = 24.dp,
)
