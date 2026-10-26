package pl.handsome.club.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class AppTypography(
    val caption: TextStyle,
    val body: TextStyle,
    val title: TextStyle,
    val display: TextStyle,
)

val DefaultAppTypography = AppTypography(
    caption = TextStyle(fontSize = 10.sp),
    body = TextStyle(fontSize = 16.sp),
    title = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    display = TextStyle(fontSize = 40.sp),
)
