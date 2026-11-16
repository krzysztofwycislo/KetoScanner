package app.carbpeek.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.R
import app.carbpeek.core.designsystem.theme.AppTheme

@Composable
fun Ui.SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(AppTheme.colors.background.subtle, AppTheme.shapes.medium)
            .padding(AppTheme.spacing.xxs)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val segmentModifier = if (isSelected) {
                Modifier
                    .shadow(AppTheme.borders.hairline, AppTheme.shapes.small)
                    .background(AppTheme.colors.background.surface, AppTheme.shapes.small)
            } else {
                Modifier
            }
            Box(
                modifier = segmentModifier
                    .clip(AppTheme.shapes.small)
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(index) })
                    .defaultMinSize(minHeight = AppTheme.sizes.control.m)
                    .padding(horizontal = AppTheme.spacing.s),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style = AppTheme.typography.label,
                    color = if (isSelected) AppTheme.colors.text.default else AppTheme.colors.text.muted,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SegmentedTogglePreview() {
    AppTheme {
        var selectedIndex by remember { mutableIntStateOf(0) }
        Ui.SegmentedToggle(
            options = listOf(
                stringResource(R.string.designsystem_preview_tab_home),
                stringResource(R.string.designsystem_preview_tab_favourites),
            ),
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it },
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
