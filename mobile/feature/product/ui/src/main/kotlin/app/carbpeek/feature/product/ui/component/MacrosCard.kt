package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.component.SegmentedToggle
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.color
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnPer100gClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnPerServingClicked
import app.carbpeek.feature.product.ui.state.MacroValueState
import app.carbpeek.feature.product.ui.state.MacrosState
import app.carbpeek.feature.product.ui.state.VerdictState

private const val MACRO_COLUMNS = 3

@Composable
internal fun MacrosCard(verdict: VerdictState, macros: MacrosState, onAction: (ProductAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.large)
            .background(AppTheme.colors.background.surface)
            .border(
                width = AppTheme.borders.hairline,
                color = AppTheme.colors.border.default,
                shape = AppTheme.shapes.large,
            ),
    ) {
        Verdict(state = verdict, onAction = onAction)
        MacroValues(state = macros, onAction = onAction)
    }
}

@Composable
private fun MacroValues(state: MacrosState, onAction: (ProductAction) -> Unit) {
    Column(
        modifier = Modifier.padding(
            start = AppTheme.spacing.xs,
            top = AppTheme.spacing.s,
            end = AppTheme.spacing.xs,
            bottom = AppTheme.spacing.xs,
        ),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        MacrosHeading(state = state, onAction = onAction)
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
            state.values.chunked(MACRO_COLUMNS).forEach { rowValues ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
                ) {
                    rowValues.forEach {
                        MacroValue(
                            state = it,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(MACRO_COLUMNS - rowValues.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MacrosHeading(state: MacrosState, onAction: (ProductAction) -> Unit) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppTheme.spacing.xs, end = AppTheme.spacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.product_macros),
            style = AppTheme.typography.section,
            color = AppTheme.colors.text.default,
            modifier = Modifier
                .padding(end = AppTheme.spacing.s)
                .heightIn(min = AppTheme.sizes.control.m)
                .wrapContentHeight(Alignment.CenterVertically),
        )
        if (state.isServingToggleVisible) {
            Ui.SegmentedToggle(
                options = listOf(stringResource(R.string.product_basis_100g), state.servingLabel.resolve()),
                selectedIndex = if (state.isPerServingSelected) 1 else 0,
                onSelect = { index -> onAction(if (index == 0) OnPer100gClicked else OnPerServingClicked) },
            )
        }
    }
}

@Composable
private fun MacroValue(state: MacroValueState, modifier: Modifier = Modifier) {
    val highlight = state.highlight
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {}
            .padding(
                start = AppTheme.spacing.xs,
                top = AppTheme.spacing.xs,
                bottom = AppTheme.spacing.xs,
            ),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
    ) {
        Text(
            text = state.label.resolve(),
            style = AppTheme.typography.label,
            color = highlight?.color() ?: AppTheme.colors.text.muted,
        )
        Text(
            text = state.value.resolve(),
            style = AppTheme.typography.title,
            color = highlight?.color() ?: AppTheme.colors.text.default,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MacrosCardPer100gPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            MacrosCard(
                verdict = VerdictState.exampleRated(),
                macros = MacrosState.example(isPerServingSelected = false, highlight = Rating.Neutral),
                onAction = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MacrosCardPerServingPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            MacrosCard(
                verdict = VerdictState.exampleRated(),
                macros = MacrosState.example(isPerServingSelected = true, highlight = Rating.Neutral),
                onAction = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MacrosCardWithoutServingPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            MacrosCard(
                verdict = VerdictState.exampleRated(),
                macros = MacrosState.example(
                    isPerServingSelected = false,
                    highlight = Rating.Neutral,
                    isServingToggleVisible = false,
                ),
                onAction = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MacrosCardInsufficientDataPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            MacrosCard(
                verdict = VerdictState.exampleInsufficientData(),
                macros = MacrosState.example(
                    isPerServingSelected = false,
                    highlight = null,
                    isServingToggleVisible = false,
                ),
                onAction = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun MacrosCardWideValuesLargeFontPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            MacrosCard(
                verdict = VerdictState.exampleRated(),
                macros = MacrosState.exampleWideValues(),
                onAction = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun MacrosCardInsufficientDataLargeFontPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            MacrosCard(
                verdict = VerdictState.exampleInsufficientData(),
                macros = MacrosState.example(
                    isPerServingSelected = false,
                    highlight = null,
                    isServingToggleVisible = false,
                ),
                onAction = {},
            )
        }
    }
}
