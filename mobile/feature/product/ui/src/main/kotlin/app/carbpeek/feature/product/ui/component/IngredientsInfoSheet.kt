package app.carbpeek.feature.product.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientsInfoDismissed

@Composable
internal fun IngredientsInfoSheet(onAction: (ProductAction) -> Unit) {
    ProductSheet(onDismiss = { onAction(OnIngredientsInfoDismissed) }) { close ->
        IngredientsInfoSheetContent(onClose = close)
    }
}

@Composable
private fun IngredientsInfoSheetContent(onClose: () -> Unit) {
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
    }
}

@Preview(widthDp = 390, heightDp = 560)
@Composable
private fun IngredientsInfoSheetPreview() {
    ProductSheetPreview {
        IngredientsInfoSheetContent(onClose = {})
    }
}

@Preview(widthDp = 360, heightDp = 570, fontScale = 2f)
@Composable
private fun IngredientsInfoSheetLargeFontPreview() {
    ProductSheetPreview {
        IngredientsInfoSheetContent(onClose = {})
    }
}

@Preview(widthDp = 891, heightDp = 339)
@Composable
private fun IngredientsInfoSheetLandscapePreview() {
    ProductSheetPreview {
        IngredientsInfoSheetContent(onClose = {})
    }
}
