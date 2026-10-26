package pl.handsome.club.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class AppShapes(
    val small: Shape,
    val circle: Shape,
)

val DefaultAppShapes = AppShapes(
    small = RoundedCornerShape(8.dp),
    circle = CircleShape,
)
