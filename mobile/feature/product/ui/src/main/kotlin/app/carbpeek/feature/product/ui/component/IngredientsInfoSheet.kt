package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientsInfoDismissed
import app.carbpeek.feature.product.ui.state.IngredientLegendState
import app.carbpeek.feature.product.ui.state.IngredientsInfoSheetState

@Composable
internal fun IngredientsInfoSheet(state: IngredientsInfoSheetState, onAction: (ProductAction) -> Unit) {
    ProductSheet(onDismiss = { onAction(OnIngredientsInfoDismissed) }) { close ->
        IngredientsInfoSheetContent(state = state, onClose = close)
    }
}

@Composable
private fun IngredientsInfoSheetContent(state: IngredientsInfoSheetState, onClose: () -> Unit) {
    ProductSheetFrame(onDismiss = onClose) {
        Text(
            text = stringResource(R.string.product_ingredients_info),
            style = AppTheme.typography.title,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = stringResource(R.string.product_ingredients_intro),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = stringResource(R.string.product_ingredients_symbols),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.default,
        )
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            state.legend.forEach {
                LegendEntry(it)
            }
        }
    }
}

@Composable
private fun LegendEntry(state: IngredientLegendState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IngredientMark(state.tone)
        Text(
            text = state.label.resolve(),
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.text.default,
        )
    }
}

@Preview(widthDp = 390, heightDp = 560)
@Composable
private fun IngredientsInfoSheetPreview() {
    ProductSheetPreview {
        IngredientsInfoSheetContent(state = IngredientsInfoSheetState.example(), onClose = {})
    }
}

@Preview(widthDp = 360, heightDp = 570, fontScale = 2f)
@Composable
private fun IngredientsInfoSheetLargeFontPreview() {
    ProductSheetPreview {
        IngredientsInfoSheetContent(state = IngredientsInfoSheetState.example(), onClose = {})
    }
}

@Preview(widthDp = 891, heightDp = 339)
@Composable
private fun IngredientsInfoSheetLandscapePreview() {
    ProductSheetPreview {
        IngredientsInfoSheetContent(state = IngredientsInfoSheetState.example(), onClose = {})
    }
}
