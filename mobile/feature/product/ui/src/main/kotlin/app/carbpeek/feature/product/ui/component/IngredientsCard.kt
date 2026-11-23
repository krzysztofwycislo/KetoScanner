package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppColors
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientsInfoClicked
import app.carbpeek.feature.product.ui.state.IngredientChipState
import app.carbpeek.feature.product.ui.state.IngredientTone
import app.carbpeek.feature.product.ui.state.IngredientsCardState

private const val MARK_ICON_FRACTION = 0.62f

@Composable
internal fun IngredientsCard(
    state: IngredientsCardState,
    onAction: (ProductAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background.surface, AppTheme.shapes.card)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.card)
            .padding(top = AppTheme.spacing.xxs, bottom = AppTheme.spacing.m),
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
        FlowRow(
            modifier = Modifier.padding(horizontal = AppTheme.spacing.m),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            state.chips.forEachIndexed { index, chip ->
                IngredientChip(chip) { onAction(OnIngredientClicked(index)) }
            }
        }
    }
}

@Composable
private fun IngredientChip(state: IngredientChipState, onClick: () -> Unit) {
    val colors = state.tone.colors()
    val description = state.description.resolve()
    Surface(
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = description },
        shape = AppTheme.shapes.extraLarge,
        color = colors.container,
        contentColor = colors.content,
        border = BorderStroke(AppTheme.borders.control, colors.border),
    ) {
        Row(
            modifier = Modifier
                .clearAndSetSemantics {}
                .defaultMinSize(minHeight = AppTheme.sizes.control.m)
                .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.xs),
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
}

@Composable
internal fun IngredientMark(tone: IngredientTone) {
    if (tone == IngredientTone.PLAIN) {
        return
    }
    val markSize = with(LocalDensity.current) { AppTheme.typography.bodyLarge.lineHeight.toDp() }
    Box(
        modifier = Modifier
            .clearAndSetSemantics {}
            .size(markSize)
            .background(tone.colors().content, AppTheme.shapes.circle),
        contentAlignment = Alignment.Center,
    ) {
        when (tone) {
            IngredientTone.ADDED_SUGAR -> MarkIcon(AppIcons.SugarCube)
            IngredientTone.SWEETENER -> MarkIcon(AppIcons.Drop)
            IngredientTone.STARCH -> MarkIcon(AppIcons.WheatEar)
            IngredientTone.MILK -> MarkIcon(AppIcons.Glass)
            IngredientTone.ADDITIVE -> MarkText(stringResource(R.string.product_ingredient_mark_additive))
            IngredientTone.PLAIN -> Unit
        }
    }
}

@Composable
private fun MarkIcon(icon: AppIcon) {
    Icon(
        imageVector = icon.imageVector,
        contentDescription = null,
        tint = AppTheme.colors.text.inverse,
        modifier = Modifier.fillMaxSize(MARK_ICON_FRACTION),
    )
}

@Composable
private fun MarkText(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.bodySmall,
        fontWeight = FontWeight.ExtraBold,
        color = AppTheme.colors.text.inverse,
    )
}

@Composable
internal fun IngredientTone.colors(): AppColors.IngredientTone = with(AppTheme.colors.ingredient) {
    when (this@colors) {
        IngredientTone.ADDED_SUGAR -> addedSugar
        IngredientTone.SWEETENER -> sweetener
        IngredientTone.STARCH -> starch
        IngredientTone.MILK -> milk
        IngredientTone.ADDITIVE -> additive
        IngredientTone.PLAIN -> plain
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
