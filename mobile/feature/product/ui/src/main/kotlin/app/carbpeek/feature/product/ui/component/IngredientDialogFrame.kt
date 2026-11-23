package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R

@Composable
internal fun IngredientDialogFrame(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.extraLarge)
            .background(AppTheme.colors.background.surface)
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
    ) {
        content()
        Ui.PrimaryButton(
            text = stringResource(R.string.product_ingredient_got_it),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
