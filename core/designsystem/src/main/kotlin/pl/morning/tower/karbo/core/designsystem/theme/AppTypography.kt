package pl.handsome.club.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import pl.handsome.club.core.designsystem.R

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
    section = style(17, 22, FontWeight.Bold),
    bodyLarge = style(16, 22, FontWeight.Medium),
    body = style(14, 20, FontWeight.Medium),
    bodyStrong = style(15, 20, FontWeight.SemiBold),
    bodySmall = style(13, 18, FontWeight.Medium),
    button = style(15, 20, FontWeight.Bold),
    label = style(13, 16, FontWeight.Bold),
    caption = style(12, 16, FontWeight.SemiBold),
)
