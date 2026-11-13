package pl.morning.tower.karbo.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.component.SegmentedToggle
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.component.color
import pl.morning.tower.karbo.core.designsystem.component.containerColor
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.product.ui.R
import pl.morning.tower.karbo.feature.product.ui.event.ProductAction
import pl.morning.tower.karbo.feature.product.ui.event.ProductAction.OnPer100gClicked
import pl.morning.tower.karbo.feature.product.ui.event.ProductAction.OnPerServingClicked
import pl.morning.tower.karbo.feature.product.ui.state.GlanceState
import pl.morning.tower.karbo.feature.product.ui.state.GlanceTileState

private const val GLANCE_COLUMNS = 3

@Composable
internal fun Glance(state: GlanceState, onAction: (ProductAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.product_at_a_glance),
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
                modifier = Modifier.weight(1f),
            )
            if (state.isServingToggleVisible) {
                Ui.SegmentedToggle(
                    options = listOf(stringResource(R.string.product_basis_100g), state.servingLabel.resolve()),
                    selectedIndex = if (state.isPerServingSelected) 1 else 0,
                    onSelect = { index -> onAction(if (index == 0) OnPer100gClicked else OnPerServingClicked) },
                )
            }
        }
        state.tiles.chunked(GLANCE_COLUMNS).forEach { rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                rowTiles.forEach {
                    GlanceTile(it, Modifier.weight(1f))
                }
                repeat(GLANCE_COLUMNS - rowTiles.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GlanceTile(state: GlanceTileState, modifier: Modifier = Modifier) {
    val highlight = state.highlight
    val tileModifier = if (highlight != null) {
        modifier.background(highlight.containerColor(), AppTheme.shapes.card)
    } else {
        modifier
            .background(AppTheme.colors.background.surface, AppTheme.shapes.card)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.card)
    }
    val contentColor = highlight?.color() ?: AppTheme.colors.text.default
    Column(
        modifier = tileModifier.padding(AppTheme.spacing.s),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
    ) {
        Text(
            text = state.label.resolve(),
            style = AppTheme.typography.caption,
            color = highlight?.color() ?: AppTheme.colors.text.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = state.value.resolve(),
            style = AppTheme.typography.title,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GlancePer100gPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            Glance(GlanceState.example(isPerServingSelected = false, highlight = Rating.Neutral)) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GlancePerServingPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            Glance(GlanceState.example(isPerServingSelected = true, highlight = Rating.Unknown)) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GlanceWithoutServingPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            Glance(
                GlanceState.example(
                    isPerServingSelected = false,
                    highlight = Rating.Positive,
                    isServingToggleVisible = false,
                ),
            ) {}
        }
    }
}
