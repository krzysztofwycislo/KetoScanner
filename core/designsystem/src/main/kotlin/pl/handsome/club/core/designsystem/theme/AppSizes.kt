package pl.handsome.club.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSizes(
    val iconS: Dp,
    val iconM: Dp,
    val button: Dp,
    val thumbnail: Dp,
    val productImageHeight: Dp,
)

val DefaultAppSizes = AppSizes(
    iconS = 24.dp,
    iconM = 36.dp,
    button = 40.dp,
    thumbnail = 70.dp,
    productImageHeight = 220.dp,
)
