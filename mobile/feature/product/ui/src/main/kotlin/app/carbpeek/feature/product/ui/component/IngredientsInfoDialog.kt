package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientsInfoDismissed
import app.carbpeek.feature.product.ui.state.IngredientLegendState
import app.carbpeek.feature.product.ui.state.IngredientsInfoDialogState

@Composable
internal fun IngredientsInfoDialog(state: IngredientsInfoDialogState, onAction: (ProductAction) -> Unit) {
    Dialog(onDismissRequest = { onAction(OnIngredientsInfoDismissed) }) {
        IngredientsInfoDialogContent(state, onAction)
    }
}

@Composable
private fun IngredientsInfoDialogContent(state: IngredientsInfoDialogState, onAction: (ProductAction) -> Unit) {
    IngredientDialogFrame(onDismiss = { onAction(OnIngredientsInfoDismissed) }) {
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
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
            Text(
                text = stringResource(R.string.product_ingredients_symbols),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.default,
            )
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

@Preview(showBackground = true)
@Composable
private fun IngredientsInfoDialogPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            IngredientsInfoDialogContent(IngredientsInfoDialogState.example(), onAction = {})
        }
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun IngredientsInfoDialogLargeFontPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            IngredientsInfoDialogContent(IngredientsInfoDialogState.example(), onAction = {})
        }
    }
}
