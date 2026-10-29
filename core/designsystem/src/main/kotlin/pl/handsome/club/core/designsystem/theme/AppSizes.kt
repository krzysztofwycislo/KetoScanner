package pl.handsome.club.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSizes(
    val iconXs: Dp,
    val iconS: Dp,
    val iconL: Dp,
    val iconM: Dp,
    val iconTile: Dp,
    val touchTarget: Dp,
    val button: Dp,
    val control: Dp,
    val rowMinHeight: Dp,
    val pillMinHeight: Dp,
    val scanButton: Dp,
    val scanButtonLift: Dp,
    val thumbnail: Dp,
    val productImageHeight: Dp,
)

val DefaultAppSizes = AppSizes(
    iconXs = 14.dp,
    iconS = 24.dp,
    iconL = 28.dp,
    iconM = 36.dp,
    iconTile = 44.dp,
    touchTarget = 44.dp,
    button = 40.dp,
    control = 48.dp,
    rowMinHeight = 64.dp,
    pillMinHeight = 28.dp,
    scanButton = 66.dp,
    scanButtonLift = 26.dp,
    thumbnail = 70.dp,
    productImageHeight = 220.dp,
)
