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
    val productPhoto: Dp,
    val scanFrameWidth: Dp,
    val scanFrameHeight: Dp,
    val aimLine: Dp,
    val messageIcon: Dp,
    val logoTile: Dp,
    val logoIcon: Dp,
    val chipIcon: Dp,
    val buttonIcon: Dp,
    val heroCard: Dp,
    val heroIllustrationWidth: Dp,
    val heroIllustrationHeight: Dp,
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
    productPhoto = 92.dp,
    scanFrameWidth = 320.dp,
    scanFrameHeight = 200.dp,
    aimLine = 3.dp,
    messageIcon = 72.dp,
    logoTile = 28.dp,
    logoIcon = 16.dp,
    chipIcon = 18.dp,
    buttonIcon = 20.dp,
    heroCard = 236.dp,
    heroIllustrationWidth = 190.dp,
    heroIllustrationHeight = 120.dp,
)
