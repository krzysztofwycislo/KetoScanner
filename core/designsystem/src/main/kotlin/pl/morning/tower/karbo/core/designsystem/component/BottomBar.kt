package pl.morning.tower.karbo.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.morning.tower.karbo.core.designsystem.R
import pl.morning.tower.karbo.core.designsystem.icon.AppIcon
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.theme.AppMotion
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme

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
    val shape = AppTheme.shapes.bottomBar
    Row(
        modifier = modifier
            .background(AppTheme.colors.background.surface, shape)
            .drawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)
                val cornerHeight = (outline as? Outline.Rounded)?.roundRect?.topLeftCornerRadius?.y ?: 0f
                clipRect(bottom = maxOf(cornerHeight, borderWidth.toPx())) {
                    drawOutline(
                        outline = outline,
                        color = borderColor,
                        style = Stroke(width = borderWidth.toPx()),
                    )
                }
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
fun Ui.AnimatedBottomBar(
    visible: Boolean,
    items: ImmutableList<BottomBarItem>,
    selectedIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val motion = AppTheme.motion
    val navigationBarHeight = WindowInsets.navigationBars.getBottom(LocalDensity.current)
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = motion.barSpec(),
            expandFrom = Alignment.Top,
            clip = false,
            initialHeight = { navigationBarHeight },
        ),
        exit = shrinkVertically(
            animationSpec = motion.barSpec(),
            shrinkTowards = Alignment.Top,
            clip = false,
            targetHeight = { navigationBarHeight },
        ),
    ) {
        val barAlpha by transition.animateFloat(
            transitionSpec = { motion.barSpec() },
            label = "bottomBarAlpha",
        ) { state ->
            if (state == EnterExitState.Visible) {
                1f
            } else {
                0f
            }
        }
        Ui.BottomBar(
            items = items,
            selectedIndex = selectedIndex,
            onItemClick = onItemClick,
            modifier = Modifier.graphicsLayer {
                alpha = barAlpha
                compositingStrategy = CompositingStrategy.ModulateAlpha
            },
        )
    }
}

@Composable
fun rememberBottomBarOverlap(visible: Boolean): Dp {
    val overlap by animateDpAsState(
        targetValue = if (visible) {
            AppTheme.sizes.scanButtonLift
        } else {
            0.dp
        },
        animationSpec = AppTheme.motion.barSpec(),
        label = "bottomBarOverlap",
    )
    return overlap
}

private fun <T> AppMotion.barSpec() = tween<T>(durationMedium, easing = easingStandard)

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
            modifier = Modifier.size(AppTheme.sizes.icon.m),
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
                .size(AppTheme.sizes.control.l)
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
                modifier = Modifier.size(AppTheme.sizes.icon.l),
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
private fun previewItems(): ImmutableList<BottomBarItem> = persistentListOf(
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
)

@Composable
private fun BottomBarPreviewContent(selectedIndex: Int) {
    AppTheme {
        Box(
            modifier = Modifier
                .background(AppTheme.colors.background.default)
                .padding(top = AppTheme.sizes.scanButtonLift),
        ) {
            Ui.BottomBar(
                items = previewItems(),
                selectedIndex = selectedIndex,
                onItemClick = {},
            )
        }
    }
}

@Composable
private fun AnimatedBottomBarPreviewContent(visible: Boolean) {
    AppTheme {
        Box(
            modifier = Modifier
                .background(AppTheme.colors.background.default)
                .padding(top = AppTheme.sizes.scanButtonLift),
        ) {
            Ui.AnimatedBottomBar(
                visible = visible,
                items = previewItems(),
                selectedIndex = 1,
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

@Preview(showBackground = true)
@Composable
private fun AnimatedBottomBarVisiblePreview() {
    AnimatedBottomBarPreviewContent(visible = true)
}

@Preview(showBackground = true)
@Composable
private fun AnimatedBottomBarHiddenPreview() {
    AnimatedBottomBarPreviewContent(visible = false)
}
