package pl.morning.tower.karbo.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import pl.morning.tower.karbo.core.designsystem.component.Rating

@Immutable
data class AppColors(
    val background: Background,
    val text: Text,
    val accent: Color,
    val favourite: Color,
    val verdict: Verdict,
    val border: Border,
    val placeholder: Color,
    val camera: Camera,
) {

    @Immutable
    data class Background(
        val default: Color,
        val surface: Color,
        val subtle: Color,
        val hero: Color,
        val scrim: Color,
    )

    @Immutable
    data class Text(
        val default: Color,
        val muted: Color,
        val subtle: Color,
        val inverse: Color,
        val accent: Color,
        val highlight: Color,
        val onHero: Color,
    )

    @Immutable
    data class Tone(
        val container: Color,
        val content: Color,
    )

    @Immutable
    data class Verdict(
        val positive: Tone,
        val neutral: Tone,
        val negative: Tone,
        val unknown: Tone,
    )

    @Immutable
    data class Border(
        val default: Color,
        val strong: Color,
    )

    @Immutable
    data class Camera(
        val background: Color,
        val control: Color,
        val controlActive: Color,
        val hint: Color,
        val dim: Color,
        val chip: Color,
        val ratingPositive: Color,
        val ratingNeutral: Color,
        val ratingNegative: Color,
        val ratingUnknown: Color,
    )
}

internal fun AppColors.Verdict.toneFor(rating: Rating): AppColors.Tone = when (rating) {
    Rating.Positive -> positive
    Rating.Neutral -> neutral
    Rating.Negative -> negative
    Rating.Unknown -> unknown
}

val LightAppColors = AppColors(
    background = AppColors.Background(
        default = Color(0xFFF6F7F4),
        surface = Color(0xFFFFFFFF),
        subtle = Color(0xFFEEF1EC),
        hero = Color(0xFF0E1411),
        scrim = Color(0x68000000),
    ),
    text = AppColors.Text(
        default = Color(0xFF15201B),
        muted = Color(0xFF56615B),
        subtle = Color(0xFF3E4943),
        inverse = Color(0xFFFFFFFF),
        accent = Color(0xFF1E6B47),
        highlight = Color(0xFF6EE7A8),
        onHero = Color(0xFFC9D3CD),
    ),
    accent = Color(0xFF1E6B47),
    favourite = Color(0xFFC2362B),
    verdict = AppColors.Verdict(
        positive = AppColors.Tone(container = Color(0xFFE3F3EA), content = Color(0xFF13663B)),
        neutral = AppColors.Tone(container = Color(0xFFFFF0D1), content = Color(0xFF7A4F00)),
        negative = AppColors.Tone(container = Color(0xFFFDE3E0), content = Color(0xFF9E2318)),
        unknown = AppColors.Tone(container = Color(0xFFEEF1EC), content = Color(0xFF3E4943)),
    ),
    border = AppColors.Border(
        default = Color(0xFFE3E6E1),
        strong = Color(0xFFD5DAD3),
    ),
    placeholder = Color(0xFFEEF1EC),
    camera = AppColors.Camera(
        background = Color(0xFF1B2420),
        control = Color(0x29FFFFFF),
        controlActive = Color(0xFFFFE58A),
        hint = Color(0xBF0E1411),
        dim = Color(0x8C0E1411),
        chip = Color(0x1FFFFFFF),
        ratingPositive = Color(0xFF8EF0BD),
        ratingNeutral = Color(0xFFFFD27A),
        ratingNegative = Color(0xFFFFA79E),
        ratingUnknown = Color(0xFFC9D3CD),
    ),
)
