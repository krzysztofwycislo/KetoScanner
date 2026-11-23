package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientDialogDismissed
import app.carbpeek.feature.product.ui.state.IngredientDialogState
import app.carbpeek.feature.product.ui.state.IngredientSectionState

@Composable
internal fun IngredientDialog(state: IngredientDialogState, onAction: (ProductAction) -> Unit) {
    Dialog(onDismissRequest = { onAction(OnIngredientDialogDismissed) }) {
        IngredientDialogContent(state, onAction)
    }
}

@Composable
private fun IngredientDialogContent(state: IngredientDialogState, onAction: (ProductAction) -> Unit) {
    val colors = state.tone.colors()
    IngredientDialogFrame(onDismiss = { onAction(OnIngredientDialogDismissed) }) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            state.tag?.let {
                Text(
                    text = it.resolve(),
                    style = AppTheme.typography.body,
                    fontWeight = FontWeight.Bold,
                    color = colors.content,
                    modifier = Modifier
                        .background(colors.container, AppTheme.shapes.pill)
                        .padding(horizontal = AppTheme.spacing.s, vertical = AppTheme.spacing.xxs),
                )
            }
            Text(
                text = state.title.resolve(),
                style = AppTheme.typography.title,
                color = AppTheme.colors.text.default,
            )
            state.labelName?.let {
                Text(
                    text = it.resolve(),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.text.subtle,
                )
            }
        }
        state.contains?.let {
            Text(
                text = it.resolve(),
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.text.default,
            )
        }
        state.sections.forEach {
            Section(it)
        }
        state.noDescription?.let {
            Text(
                text = it.resolve(),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.default,
            )
        }
    }
}

@Composable
private fun Section(state: IngredientSectionState) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        Text(
            text = state.heading.resolve(),
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = state.text.resolve(),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.default,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientDialogPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            IngredientDialogContent(IngredientDialogState.example(), onAction = {})
        }
    }
}

@Preview(showBackground = true, fontScale = 2f)
@Composable
private fun IngredientDialogLargeFontPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            IngredientDialogContent(IngredientDialogState.example(), onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientDialogCompoundPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            IngredientDialogContent(IngredientDialogState.exampleCompound(), onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IngredientDialogNoDescriptionPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            IngredientDialogContent(IngredientDialogState.exampleNoDescription(), onAction = {})
        }
    }
}
