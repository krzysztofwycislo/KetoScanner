package app.carbpeek.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val TIGHT_TEXT_SPACE = 300.dp

@Composable
@ReadOnlyComposable
fun isTextSpaceTight(): Boolean = isTextSpaceTight(
    windowWidth = LocalWindowInfo.current.containerDpSize.width,
    fontScale = LocalDensity.current.fontScale,
)

internal fun isTextSpaceTight(windowWidth: Dp, fontScale: Float): Boolean = windowWidth / fontScale <= TIGHT_TEXT_SPACE
