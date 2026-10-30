package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.theme.AppTheme

private const val HERO_FRAME =
    "M2 26V12A10 10 0 0 1 12 2h14M164 2h14a10 10 0 0 1 10 10v14M188 94v14a10 10 0 0 1-10 10h-14M26 118H12A10 10 0 0 1 2 108V94"
private const val HERO_BARS = "M40 30v60M54 30v60M64 30v60M82 30v60M92 30v60M108 30v60M122 30v60M134 30v60M150 30v60"
private const val HERO_VIEWPORT_WIDTH = 190f
private const val HERO_VIEWPORT_HEIGHT = 120f
private const val HERO_ILLUSTRATION_ALPHA = 0.9f
private const val HERO_BARS_ALPHA = 0.5f

@Composable
fun Ui.HeroCard(
    eyebrow: String,
    title: String,
    body: String,
    actionText: String,
    actionIcon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppTheme.sizes.heroCard)
            .clip(AppTheme.shapes.extraLarge)
            .background(colors.background.hero)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(AppTheme.spacing.l),
    ) {
        HeroIllustration(
            frameColor = colors.text.highlight,
            barsColor = colors.text.inverse,
            width = AppTheme.sizes.heroIllustrationWidth,
            height = AppTheme.sizes.heroIllustrationHeight,
            modifier = Modifier.align(Alignment.TopEnd),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = eyebrow.uppercase(),
                style = AppTheme.typography.label,
                color = colors.text.highlight,
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
                Text(
                    text = title,
                    style = AppTheme.typography.headline,
                    color = colors.text.inverse,
                )
                Text(
                    text = body,
                    style = AppTheme.typography.body,
                    color = colors.text.onHero,
                )
            }
            Row(
                modifier = Modifier
                    .height(AppTheme.sizes.control)
                    .background(colors.text.highlight, AppTheme.shapes.control)
                    .padding(horizontal = AppTheme.spacing.m),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    tint = colors.background.hero,
                    modifier = Modifier.size(AppTheme.sizes.buttonIcon),
                )
                Text(
                    text = actionText,
                    style = AppTheme.typography.button,
                    color = colors.background.hero,
                )
            }
        }
    }
}

@Composable
private fun HeroIllustration(
    frameColor: Color,
    barsColor: Color,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val illustration = remember(frameColor, barsColor, width, height) {
        ImageVector.Builder(
            name = "HeroIllustration",
            defaultWidth = width,
            defaultHeight = height,
            viewportWidth = HERO_VIEWPORT_WIDTH,
            viewportHeight = HERO_VIEWPORT_HEIGHT,
        ).apply {
            addPath(
                pathData = addPathNodes(HERO_FRAME),
                stroke = SolidColor(frameColor),
                strokeLineWidth = 4f,
                strokeLineCap = StrokeCap.Round,
            )
            addPath(
                pathData = addPathNodes(HERO_BARS),
                stroke = SolidColor(barsColor),
                strokeAlpha = HERO_BARS_ALPHA,
                strokeLineWidth = 5f,
                strokeLineCap = StrokeCap.Round,
            )
        }.build()
    }
    Image(
        painter = rememberVectorPainter(illustration),
        contentDescription = null,
        modifier = modifier
            .size(width, height)
            .alpha(HERO_ILLUSTRATION_ALPHA),
    )
}

@Preview(showBackground = true)
@Composable
private fun HeroCardPreview() {
    AppTheme {
        Ui.HeroCard(
            eyebrow = stringResource(R.string.designsystem_preview_rating),
            title = stringResource(R.string.designsystem_preview_title),
            body = stringResource(R.string.designsystem_preview_body),
            actionText = stringResource(R.string.designsystem_preview_button),
            actionIcon = AppIcons.Scan,
            onClick = {},
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
