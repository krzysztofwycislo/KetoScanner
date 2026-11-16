package app.carbpeek.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.theme.AppTheme

@Composable
fun Ui.CoachMark(
    visible: Boolean,
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = AppTheme.motion
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(tween(motion.durationMedium, easing = motion.easingStandard)) +
            fadeIn(tween(motion.durationMedium, easing = motion.easingStandard)),
        exit = shrinkVertically(tween(motion.durationMedium, easing = motion.easingStandard)) +
            fadeOut(tween(motion.durationShort, easing = motion.easingStandard)),
    ) {
        CoachMarkCard(
            title = title,
            body = body,
            actionLabel = actionLabel,
            onAction = onAction,
        )
    }
}

@Composable
private fun CoachMarkCard(
    title: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { paneTitle = title }
            .background(colors.camera.tipSurface, AppTheme.shapes.large)
            .border(AppTheme.borders.control, colors.text.highlight, AppTheme.shapes.large)
            .padding(horizontal = spacing.l, vertical = spacing.m),
        verticalArrangement = Arrangement.spacedBy(spacing.s),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.s)) {
            Box(
                modifier = Modifier
                    .size(AppTheme.sizes.control.xs)
                    .background(colors.text.highlight, AppTheme.shapes.circle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Info.imageVector,
                    contentDescription = null,
                    tint = colors.background.hero,
                    modifier = Modifier.size(AppTheme.sizes.icon.s),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Text(
                    text = title,
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.text.inverse,
                )
                Text(
                    text = body,
                    style = AppTheme.typography.bodySmall,
                    color = colors.text.onHero,
                )
            }
        }
        Button(
            onClick = onAction,
            modifier = Modifier
                .align(Alignment.End)
                .heightIn(min = AppTheme.sizes.control.m),
            shape = AppTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.text.highlight,
                contentColor = colors.background.hero,
            ),
            contentPadding = PaddingValues(horizontal = spacing.l),
        ) {
            Text(
                text = actionLabel,
                style = AppTheme.typography.button,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Preview
@Composable
private fun CoachMarkPreview() {
    AppTheme {
        Box(
            modifier = Modifier
                .background(AppTheme.colors.camera.background)
                .padding(AppTheme.spacing.m),
        ) {
            Ui.CoachMark(
                visible = true,
                title = stringResource(R.string.designsystem_preview_title),
                body = stringResource(R.string.designsystem_preview_note),
                actionLabel = stringResource(R.string.designsystem_preview_button),
                onAction = {},
            )
        }
    }
}
