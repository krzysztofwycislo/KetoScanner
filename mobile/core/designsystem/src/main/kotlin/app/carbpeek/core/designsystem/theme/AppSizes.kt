package app.carbpeek.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSizes(
    val icon: Icon,
    val control: Control,
    val iconTile: Dp,
    val iconDisc: Dp,
    val iconDiscCompact: Dp,
    val iconDiscLarge: Dp,
    val productPhoto: Dp,
    val overlayCardMaxWidth: Dp,
    val cameraHintMaxWidth: Dp,
    val scanButtonLift: Dp,
) {

    @Immutable
    data class Icon(
        val xs: Dp,
        val s: Dp,
        val m: Dp,
        val l: Dp,
        val xl: Dp,
    )

    @Immutable
    data class Control(
        val xs: Dp,
        val s: Dp,
        val m: Dp,
        val l: Dp,
        val xl: Dp,
    )
}

val DefaultAppSizes = AppSizes(
    icon = AppSizes.Icon(
        xs = 16.dp,
        s = 20.dp,
        m = 24.dp,
        l = 28.dp,
        xl = 36.dp,
    ),
    control = AppSizes.Control(
        xs = 28.dp,
        s = 40.dp,
        m = 48.dp,
        l = 64.dp,
        xl = 72.dp,
    ),
    iconTile = 44.dp,
    iconDisc = 72.dp,
    iconDiscCompact = 52.dp,
    iconDiscLarge = 88.dp,
    productPhoto = 92.dp,
    overlayCardMaxWidth = 560.dp,
    cameraHintMaxWidth = 300.dp,
    scanButtonLift = 24.dp,
)
