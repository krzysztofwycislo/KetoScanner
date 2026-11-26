package app.carbpeek.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.theme.AppTheme

@Composable
fun Ui.SwitchCard(
    title: String,
    note: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AppTheme.shapes.card
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppTheme.sizes.control.xl)
            .clip(shape)
            .background(AppTheme.colors.background.surface)
            .border(width = AppTheme.borders.control, color = AppTheme.colors.border.default, shape = shape)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colors.text.default,
            )
            Text(
                text = note,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.muted,
            )
        }
        SwitchTrack(checked)
    }
}

@Composable
private fun SwitchTrack(checked: Boolean) {
    val sizes = AppTheme.sizes.toggle
    val padding = AppTheme.spacing.xxs
    val duration = AppTheme.motion.durationShort
    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            AppTheme.colors.accent
        } else {
            AppTheme.colors.switchOff
        },
        animationSpec = tween(duration),
        label = "SwitchTrackColor",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) {
            sizes.trackWidth - sizes.thumb - padding * 2
        } else {
            0.dp
        },
        animationSpec = tween(duration),
        label = "SwitchThumbOffset",
    )
    Box(
        modifier = Modifier
            .size(width = sizes.trackWidth, height = sizes.trackHeight)
            .background(color = trackColor, shape = AppTheme.shapes.pill)
            .padding(padding),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(x = thumbOffset.roundToPx(), y = 0) }
                .size(sizes.thumb)
                .background(color = AppTheme.colors.background.surface, shape = AppTheme.shapes.circle),
        )
    }
}

@Composable
private fun SwitchCardExample(initiallyChecked: Boolean) {
    AppTheme {
        var checked by remember { mutableStateOf(initiallyChecked) }
        Ui.SwitchCard(
            title = stringResource(R.string.designsystem_preview_title),
            note = stringResource(R.string.designsystem_preview_note),
            checked = checked,
            onCheckedChange = { checked = it },
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SwitchCardOnPreview() {
    SwitchCardExample(initiallyChecked = true)
}

@Preview(showBackground = true)
@Composable
private fun SwitchCardOffPreview() {
    SwitchCardExample(initiallyChecked = false)
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun SwitchCardLargeFontPreview() {
    SwitchCardExample(initiallyChecked = true)
}
