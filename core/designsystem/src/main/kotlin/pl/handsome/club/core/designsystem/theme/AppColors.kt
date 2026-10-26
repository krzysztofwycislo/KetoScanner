package pl.handsome.club.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val background: Background,
    val text: Text,
    val accent: Color,
    val result: Result,
    val border: Border,
    val placeholder: Color,
) {

    @Immutable
    data class Background(
        val default: Color,
        val scrim: Color,
    )

    @Immutable
    data class Text(
        val default: Color,
        val inverse: Color,
        val accent: Color,
    )

    @Immutable
    data class Result(
        val positive: Color,
        val neutral: Color,
        val negative: Color,
    )

    @Immutable
    data class Border(
        val default: Color,
    )
}

val LightAppColors = AppColors(
    background = AppColors.Background(
        default = Color(0xFFFFFFFF),
        scrim = Color(0x68000000),
    ),
    text = AppColors.Text(
        default = Color(0xFF000000),
        inverse = Color(0xFFFFFFFF),
        accent = Color(0xFF64B5F6),
    ),
    accent = Color(0xFF64B5F6),
    result = AppColors.Result(
        positive = Color(0xFF00A809),
        neutral = Color(0xFFA89700),
        negative = Color(0xFFA80000),
    ),
    border = AppColors.Border(
        default = Color(0xFFDCDCDC),
    ),
    placeholder = Color(0xFFDCDCDC),
)
