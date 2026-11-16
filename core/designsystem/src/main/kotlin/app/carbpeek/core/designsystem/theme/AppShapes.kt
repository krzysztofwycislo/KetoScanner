package app.carbpeek.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class AppShapes(
    val small: Shape,
    val medium: Shape,
    val control: Shape,
    val card: Shape,
    val large: Shape,
    val extraLarge: Shape,
    val pill: Shape,
    val circle: Shape,
    val bottomBar: Shape,
)

val DefaultAppShapes = AppShapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    control = RoundedCornerShape(14.dp),
    card = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(26.dp),
    pill = RoundedCornerShape(percent = 50),
    circle = CircleShape,
    bottomBar = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
)
