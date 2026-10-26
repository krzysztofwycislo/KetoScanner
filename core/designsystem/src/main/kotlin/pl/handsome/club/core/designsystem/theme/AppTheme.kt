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

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }
private val LocalAppTypography = staticCompositionLocalOf { DefaultAppTypography }
private val LocalAppSpacing = staticCompositionLocalOf { DefaultAppSpacing }
private val LocalAppSizes = staticCompositionLocalOf { DefaultAppSizes }
private val LocalAppShapes = staticCompositionLocalOf { DefaultAppShapes }
private val LocalAppBorders = staticCompositionLocalOf { DefaultAppBorders }

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
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography = typography.toMaterialTypography(),
            content = content,
        )
    }
}

private fun AppColors.toMaterialColorScheme() = lightColorScheme(
    primary = text.default,
    onPrimary = text.inverse,
    secondary = accent,
    background = background.default,
    onBackground = text.default,
    surface = background.default,
    onSurface = text.default,
    error = result.negative,
    outline = border.default,
)

private fun AppTypography.toMaterialTypography() = Typography(
    bodyLarge = body,
    titleLarge = title,
    labelSmall = caption,
    displayMedium = display,
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
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                listOf(
                    colors.background.default,
                    colors.background.scrim,
                    colors.text.default,
                    colors.accent,
                    colors.result.positive,
                    colors.result.neutral,
                    colors.result.negative,
                    colors.border.default,
                    colors.placeholder,
                ).forEach { ColorSwatch(it) }
            }
            Text("Caption", style = AppTheme.typography.caption)
            Text("Body", style = AppTheme.typography.body)
            Text("Title", style = AppTheme.typography.title)
            Text("Display", style = AppTheme.typography.display)
        }
    }
}

@Composable
private fun ColorSwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(AppTheme.sizes.iconS)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.small)
            .background(color, AppTheme.shapes.small),
    )
}
