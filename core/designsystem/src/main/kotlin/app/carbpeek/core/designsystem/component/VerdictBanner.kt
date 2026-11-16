package app.carbpeek.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme

@Composable
fun Ui.VerdictBanner(
    rating: Rating,
    title: UiText,
    details: List<UiText>,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val color = rating.color()
    val motion = AppTheme.motion
    val highlight = AppTheme.colors.text.highlight
    val ringColor by animateColorAsState(
        targetValue = if (highlighted) highlight else highlight.copy(alpha = 0f),
        animationSpec = tween(durationMillis = motion.durationMedium, easing = motion.easingStandard),
        label = "VerdictBannerHighlight",
    )
    CompositionLocalProvider(LocalContentColor provides color) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .highlightRing(
                    color = { ringColor },
                    ringWidth = AppTheme.borders.highlightRing,
                    shape = AppTheme.shapes.large,
                )
                .background(rating.containerColor(), AppTheme.shapes.large)
                .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.l),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(AppTheme.sizes.iconTile)
                        .background(color, AppTheme.shapes.circle),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = rating.markIcon().imageVector,
                        contentDescription = null,
                        tint = AppTheme.colors.text.inverse,
                        modifier = Modifier.size(AppTheme.sizes.icon.m),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
                    Text(text = title.resolve(), style = AppTheme.typography.headline, color = color)
                    details.forEach {
                        Text(text = it.resolve(), style = AppTheme.typography.bodyStrong, color = color)
                    }
                }
            }
            content()
        }
    }
}

private fun Modifier.highlightRing(color: () -> Color, ringWidth: Dp, shape: Shape): Modifier =
    drawBehind {
        val ringColor = color()
        if (ringColor.alpha > 0f) {
            val strokeWidth = ringWidth.toPx()
            val inset = strokeWidth + strokeWidth / 2
            val outline = shape.createOutline(
                size = Size(width = size.width + inset * 2, height = size.height + inset * 2),
                layoutDirection = layoutDirection,
                density = this,
            )
            translate(left = -inset, top = -inset) {
                drawOutline(outline = outline, color = ringColor, style = Stroke(width = strokeWidth))
            }
        }
    }

@Preview(showBackground = true)
@Composable
private fun VerdictBannerPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        ) {
            Rating.entries.forEach {
                Ui.VerdictBanner(
                    rating = it,
                    title = UiText.Res(R.string.designsystem_preview_rating),
                    details = listOf(UiText.Res(R.string.designsystem_preview_share, UiArg.Numeric.Percent(0.19))),
                    highlighted = it == Rating.Negative,
                ) {
                    Text(
                        text = UiText.Res(R.string.designsystem_preview_body).resolve(),
                        style = AppTheme.typography.body,
                    )
                }
            }
        }
    }
}
