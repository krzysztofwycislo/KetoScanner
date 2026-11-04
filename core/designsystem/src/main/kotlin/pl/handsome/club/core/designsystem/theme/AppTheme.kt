package pl.handsome.club.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }
private val LocalAppTypography = staticCompositionLocalOf { DefaultAppTypography }
private val LocalAppSpacing = staticCompositionLocalOf { DefaultAppSpacing }
private val LocalAppSizes = staticCompositionLocalOf { DefaultAppSizes }
private val LocalAppShapes = staticCompositionLocalOf { DefaultAppShapes }
private val LocalAppBorders = staticCompositionLocalOf { DefaultAppBorders }
private val LocalAppMotion = staticCompositionLocalOf { DefaultAppMotion }

object AppTheme {

    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current

    val typography: AppTypography
        @Composable @ReadOnlyComposable get() = LocalAppTypography.current

    val spacing: AppSpacing
        @Composable @ReadOnlyComposable get() = LocalAppSpacing.current

    val sizes: AppSizes
        @Composable @ReadOnlyComposable get() = LocalAppSizes.current

    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = LocalAppShapes.current

    val borders: AppBorders
        @Composable @ReadOnlyComposable get() = LocalAppBorders.current

    val motion: AppMotion
        @Composable @ReadOnlyComposable get() = LocalAppMotion.current
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colors = LightAppColors
    val typography = DefaultAppTypography
    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTypography provides typography,
        LocalAppSpacing provides DefaultAppSpacing,
        LocalAppSizes provides DefaultAppSizes,
        LocalAppShapes provides DefaultAppShapes,
        LocalAppBorders provides DefaultAppBorders,
        LocalAppMotion provides DefaultAppMotion,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = typography.toMaterialTypography(),
            content = content,
        )
    }
}

private fun AppColors.toMaterialColorScheme() = lightColorScheme(
    primary = accent,
    onPrimary = text.inverse,
    secondary = accent,
    onSecondary = text.inverse,
    background = background.default,
    onBackground = text.default,
    surface = background.surface,
    onSurface = text.default,
    surfaceVariant = background.subtle,
    onSurfaceVariant = text.muted,
    outline = border.strong,
    outlineVariant = border.default,
    error = verdict.negative.content,
)

private fun AppTypography.toMaterialTypography() = Typography(
    displayLarge = display,
    displayMedium = display,
    displaySmall = display,
    headlineLarge = headline,
    headlineMedium = headline,
    headlineSmall = headline,
    titleLarge = title,
    titleMedium = section,
    titleSmall = bodyStrong,
    bodyLarge = bodyLarge,
    bodyMedium = body,
    bodySmall = bodySmall,
    labelLarge = button,
    labelMedium = label,
    labelSmall = caption,
)

@Preview(showBackground = true)
@Composable
private fun AppThemePreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            val colors = AppTheme.colors
            SwatchRow(
                listOf(
                    colors.background.default,
                    colors.background.surface,
                    colors.background.subtle,
                    colors.background.hero,
                    colors.background.scrim,
                    colors.accent,
                    colors.border.default,
                    colors.border.strong,
                    colors.placeholder,
                ),
            )
            SwatchRow(
                listOf(
                    colors.text.default,
                    colors.text.muted,
                    colors.text.subtle,
                    colors.text.inverse,
                    colors.text.accent,
                    colors.text.highlight,
                ),
            )
            with(colors.verdict) {
                SwatchRow(
                    listOf(
                        positive.container,
                        positive.content,
                        neutral.container,
                        neutral.content,
                        negative.container,
                        negative.content,
                        unknown.container,
                        unknown.content,
                    ),
                )
            }
            val sample = UiText.Res(R.string.designsystem_preview_title).resolve()
            with(AppTheme.typography) {
                listOf(
                    display,
                    headline,
                    title,
                    section,
                    bodyLarge,
                    body,
                    bodyStrong,
                    bodySmall,
                    button,
                    label,
                    caption,
                )
            }.forEach {
                Text(sample, style = it, color = colors.text.default)
            }
        }
    }
}

@Composable
private fun SwatchRow(colors: List<Color>) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        colors.forEach {
            ColorSwatch(it)
        }
    }
}

@Composable
private fun ColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(AppTheme.sizes.icon.m)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.small)
            .background(color, AppTheme.shapes.small),
    )
}
