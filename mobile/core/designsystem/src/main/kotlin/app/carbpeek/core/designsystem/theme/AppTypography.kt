package app.carbpeek.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.carbpeek.core.designsystem.R

internal val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

@Immutable
data class AppTypography(
    val display: TextStyle,
    val headline: TextStyle,
    val title: TextStyle,
    val section: TextStyle,
    val bodyLarge: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val bodySmall: TextStyle,
    val button: TextStyle,
    val label: TextStyle,
    val caption: TextStyle,
    val micro: TextStyle,
)

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: TextUnit = TextUnit.Unspecified,
) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacing,
)

val DefaultAppTypography = AppTypography(
    display = style(28, 34, FontWeight.ExtraBold, (-0.5).sp),
    headline = style(22, 28, FontWeight.ExtraBold, (-0.3).sp),
    title = style(20, 26, FontWeight.Bold),
    section = style(size = 18, lineHeight = 23, weight = FontWeight.Bold),
    bodyLarge = style(16, 22, FontWeight.Medium),
    body = style(size = 15, lineHeight = 20, weight = FontWeight.Medium),
    bodyStrong = style(size = 16, lineHeight = 22, weight = FontWeight.SemiBold),
    bodySmall = style(size = 14, lineHeight = 19, weight = FontWeight.Medium),
    button = style(size = 16, lineHeight = 20, weight = FontWeight.Bold),
    label = style(size = 14, lineHeight = 18, weight = FontWeight.Bold),
    caption = style(size = 13, lineHeight = 17, weight = FontWeight.SemiBold),
    micro = style(size = 12, lineHeight = 16, weight = FontWeight.SemiBold),
)
