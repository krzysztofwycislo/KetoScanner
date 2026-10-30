package pl.handsome.club.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.handsome.club.core.designsystem.R
import pl.handsome.club.core.designsystem.icon.AppIcon
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.theme.AppTheme

@Immutable
data class BottomBarItem(
    val label: String,
    val icon: AppIcon,
    val selectedIcon: AppIcon = icon,
    val isProminent: Boolean = false,
)

@Composable
fun Ui.BottomBar(
    items: ImmutableList<BottomBarItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = AppTheme.colors.border.default
    val borderWidth = AppTheme.borders.hairline
    Row(
        modifier = modifier
            .background(AppTheme.colors.background.surface)
            .drawBehind {
                val y = borderWidth.toPx() / 2
                drawLine(borderColor, Offset(0f, y), Offset(size.width, y), borderWidth.toPx())
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = AppTheme.spacing.m, end = AppTheme.spacing.m, bottom = AppTheme.spacing.xs),
        verticalAlignment = Alignment.Bottom,
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            if (item.isProminent) {
                val interactionSource = remember { MutableInteractionSource() }
                val itemModifier = Modifier
                    .weight(1f)
                    .selectable(
                        selected = selected,
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Tab,
                        onClick = { onItemClick(index) },
                    )
                ProminentItem(item, selected, AppTheme.sizes.scanButtonLift, interactionSource, itemModifier)
            } else {
                val itemModifier = Modifier
                    .weight(1f)
                    .selectable(selected = selected, onClick = { onItemClick(index) }, role = Role.Tab)
                RegularItem(item, selected, itemModifier)
            }
        }
    }
}

@Composable
private fun RegularItem(item: BottomBarItem, selected: Boolean, modifier: Modifier) {
    val color = if (selected) AppTheme.colors.accent else AppTheme.colors.text.muted
    Column(
        modifier = modifier.padding(top = AppTheme.spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
    ) {
        Icon(
            imageVector = (if (selected) item.selectedIcon else item.icon).imageVector,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(AppTheme.sizes.iconS),
        )
        Text(
            text = item.label,
            style = AppTheme.typography.caption,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else null,
            maxLines = 1,
        )
    }
}

@Composable
private fun ProminentItem(
    item: BottomBarItem,
    selected: Boolean,
    lift: Dp,
    interactionSource: MutableInteractionSource,
    modifier: Modifier,
) {
    Column(
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val liftPx = lift.roundToPx().coerceAtMost(placeable.height)
                layout(placeable.width, placeable.height - liftPx) {
                    placeable.place(0, -liftPx)
                }
            }
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.scanButton)
                .clip(AppTheme.shapes.circle)
                .indication(interactionSource, ripple())
                .background(AppTheme.colors.accent, AppTheme.shapes.circle)
                .border(AppTheme.borders.scanButtonRing, AppTheme.colors.background.default, AppTheme.shapes.circle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = (if (selected) item.selectedIcon else item.icon).imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.inverse,
                modifier = Modifier.size(AppTheme.sizes.iconL),
            )
        }
        Text(
            text = item.label,
            style = AppTheme.typography.caption,
            color = if (selected) AppTheme.colors.accent else AppTheme.colors.text.default,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun BottomBarPreviewContent(selectedIndex: Int) {
    AppTheme {
        Box(
            modifier = Modifier
                .background(AppTheme.colors.background.default)
                .padding(top = AppTheme.sizes.scanButtonLift),
        ) {
            Ui.BottomBar(
                items = persistentListOf(
                    BottomBarItem(stringResource(R.string.designsystem_preview_tab_home), AppIcons.Home),
                    BottomBarItem(
                        stringResource(R.string.designsystem_preview_tab_scan),
                        AppIcons.Scan,
                        isProminent = true,
                    ),
                    BottomBarItem(
                        stringResource(R.string.designsystem_preview_tab_favourites),
                        AppIcons.Heart,
                        AppIcons.HeartFilled,
                    ),
                ),
                selectedIndex = selectedIndex,
                onItemClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomBarHomeSelectedPreview() {
    BottomBarPreviewContent(selectedIndex = 0)
}

@Preview(showBackground = true)
@Composable
private fun BottomBarScanSelectedPreview() {
    BottomBarPreviewContent(selectedIndex = 1)
}
