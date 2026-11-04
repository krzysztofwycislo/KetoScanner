package pl.morning.tower.karbo.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.R
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme

@Composable
fun Ui.RadioCard(
    title: String,
    note: String,
    trailing: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = AppTheme.shapes.card
    val border = if (selected) {
        AppTheme.borders.selected
    } else {
        AppTheme.borders.control
    }
    val borderColor = if (selected) {
        AppTheme.colors.accent
    } else {
        AppTheme.colors.border.default
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = AppTheme.sizes.control.xl)
            .clip(shape)
            .background(AppTheme.colors.background.surface)
            .border(border, borderColor, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.s),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioDot(selected)
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
        if (trailing != null) {
            Text(
                text = trailing,
                style = AppTheme.typography.body.copy(fontWeight = FontWeight.ExtraBold),
                color = AppTheme.colors.text.default,
            )
        }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    val ringColor = if (selected) {
        AppTheme.colors.accent
    } else {
        AppTheme.colors.border.strong
    }
    Box(
        modifier = Modifier
            .size(AppTheme.sizes.icon.m)
            .border(AppTheme.borders.selected, ringColor, AppTheme.shapes.circle),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(AppTheme.sizes.icon.m / 2)
                    .background(AppTheme.colors.accent, AppTheme.shapes.circle),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RadioCardPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .padding(AppTheme.spacing.m)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Ui.RadioCard(
                title = stringResource(R.string.designsystem_preview_title),
                note = stringResource(R.string.designsystem_preview_body),
                trailing = stringResource(R.string.designsystem_preview_carbs, "20"),
                selected = true,
                onClick = {},
            )
            Ui.RadioCard(
                title = stringResource(R.string.designsystem_preview_title),
                note = stringResource(R.string.designsystem_preview_body),
                trailing = null,
                selected = false,
                onClick = {},
            )
        }
    }
}
