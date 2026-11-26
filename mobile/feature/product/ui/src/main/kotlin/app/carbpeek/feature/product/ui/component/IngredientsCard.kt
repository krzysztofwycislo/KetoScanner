package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientsInfoClicked
import app.carbpeek.feature.product.ui.state.IngredientsCardState

@Composable
internal fun IngredientsCard(
    state: IngredientsCardState,
    onAction: (ProductAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.large)
            .background(color = AppTheme.colors.background.surface, shape = AppTheme.shapes.large)
            .border(
                width = AppTheme.borders.hairline,
                color = AppTheme.colors.border.default,
                shape = AppTheme.shapes.large,
            )
            .padding(top = AppTheme.spacing.xxs),
    ) {
        Row(
            modifier = Modifier.padding(
                start = AppTheme.spacing.m,
                end = AppTheme.spacing.xxs,
                bottom = AppTheme.spacing.s,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.product_ingredients_title),
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { onAction(OnIngredientsInfoClicked) },
                modifier = Modifier.size(AppTheme.sizes.control.m),
            ) {
                Icon(
                    imageVector = AppIcons.Info.imageVector,
                    contentDescription = stringResource(R.string.product_ingredients_info),
                    tint = AppTheme.colors.text.subtle,
                    modifier = Modifier.size(AppTheme.sizes.icon.m),
                )
            }
        }
        IngredientRows(
            rows = state.rows,
            onClick = { index -> onAction(OnIngredientClicked(index)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientsCardPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            IngredientsCard(IngredientsCardState.example(), onAction = {})
        }
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun IngredientsCardLargeFontPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            IngredientsCard(IngredientsCardState.example(), onAction = {})
        }
    }
}
