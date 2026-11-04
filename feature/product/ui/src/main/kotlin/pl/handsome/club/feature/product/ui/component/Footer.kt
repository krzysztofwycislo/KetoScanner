package pl.handsome.club.feature.product.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.product.ui.R
import pl.handsome.club.feature.product.ui.event.ProductAction
import pl.handsome.club.feature.product.ui.event.ProductAction.OnOpenFoodFactsClicked

@Composable
internal fun Footer(dataSource: UiText, onAction: (ProductAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dataSource.resolve(),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.text.muted,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = AppTheme.sizes.touchTarget)
                .clip(AppTheme.shapes.small)
                .clickable(role = Role.Button) { onAction(OnOpenFoodFactsClicked) }
                .padding(horizontal = AppTheme.spacing.s),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.product_fix_on_open_food_facts),
                style = AppTheme.typography.label,
                color = AppTheme.colors.text.accent,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FooterPreview() {
    AppTheme {
        Footer(exampleDataSource) {}
    }
}
