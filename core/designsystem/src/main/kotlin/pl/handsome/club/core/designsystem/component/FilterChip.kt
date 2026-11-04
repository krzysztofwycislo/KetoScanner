package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.theme.AppTheme

@Composable
fun Ui.FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = AppTheme.shapes.pill
    val borderModifier = if (selected) Modifier
    else Modifier.border(AppTheme.borders.control, AppTheme.colors.border.strong, shape)
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = AppTheme.sizes.control.m)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(AppTheme.sizes.control.s)
                .clip(shape)
                .background(if (selected) AppTheme.colors.accent else AppTheme.colors.background.surface)
                .then(borderModifier)
                .indication(interactionSource, ripple())
                .padding(horizontal = AppTheme.spacing.s),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = AppTheme.typography.label,
                color = if (selected) AppTheme.colors.text.inverse else AppTheme.colors.text.default,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipPreview() {
    AppTheme {
        Row(
            modifier = Modifier.padding(AppTheme.spacing.m),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            Ui.FilterChip(text = stringResource(R.string.designsystem_preview_title), selected = true, onClick = {})
            Ui.FilterChip(text = stringResource(R.string.designsystem_preview_body), selected = false, onClick = {})
        }
    }
}
