package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppColors
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientBackClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientContentClicked
import app.carbpeek.feature.product.ui.event.ProductAction.OnIngredientSheetDismissed
import app.carbpeek.feature.product.ui.state.IngredientBackState
import app.carbpeek.feature.product.ui.state.IngredientRowState
import app.carbpeek.feature.product.ui.state.IngredientSectionState
import app.carbpeek.feature.product.ui.state.IngredientSheetState
import app.carbpeek.feature.product.ui.state.IngredientTone
import kotlinx.collections.immutable.PersistentList

@Composable
internal fun IngredientSheet(state: IngredientSheetState, onAction: (ProductAction) -> Unit) {
    ProductSheet(
        onDismiss = { onAction(OnIngredientSheetDismissed) },
        onBack = state.back?.let {
            { onAction(OnIngredientBackClicked) }
        },
    ) { close ->
        IngredientSheetContent(state = state, onAction = onAction, onClose = close)
    }
}

@Composable
private fun IngredientSheetContent(
    state: IngredientSheetState,
    onAction: (ProductAction) -> Unit,
    onClose: () -> Unit,
) {
    val colors = state.tone.colors()
    val compoundScrollState = rememberScrollState()
    val scrollState = if (state.back == null) {
        compoundScrollState
    } else {
        rememberScrollState()
    }
    ProductSheetFrame(
        onDismiss = onClose,
        scrollState = scrollState,
        header = state.back?.let { back ->
            {
                BackRow(back) { onAction(OnIngredientBackClicked) }
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
            state.tag?.let {
                Text(
                    text = it.resolve(),
                    style = AppTheme.typography.bodySmall,
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
        if (state.contents.isNotEmpty()) {
            Contents(state.contents) { index -> onAction(OnIngredientContentClicked(index)) }
        }
        state.sections.forEach {
            Section(it)
        }
    }
}

@Composable
private fun BackRow(state: IngredientBackState, onClick: () -> Unit) {
    val description = state.description.resolve()
    Box(
        modifier = Modifier
            .padding(
                start = AppTheme.spacing.s,
                end = AppTheme.spacing.s,
                bottom = AppTheme.spacing.xxs,
            )
            .clip(AppTheme.shapes.pill)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
    ) {
        Row(
            modifier = Modifier
                .clearAndSetSemantics {}
                .heightIn(min = AppTheme.sizes.control.m)
                .padding(start = AppTheme.spacing.xs, end = AppTheme.spacing.s),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = AppIcons.Back.imageVector,
                contentDescription = null,
                tint = AppTheme.colors.text.subtle,
                modifier = Modifier.size(AppTheme.sizes.icon.m),
            )
            Text(
                text = state.label.resolve(),
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.text.subtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Contents(rows: PersistentList<IngredientRowState>, onClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        Heading(stringResource(R.string.product_ingredient_contains))
        Column {
            IngredientRows(
                rows = rows,
                onClick = onClick,
                sidePadding = 0.dp,
            )
            IngredientRowLine()
        }
    }
}

@Composable
private fun Section(state: IngredientSectionState) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        Heading(state.heading.resolve())
        Text(
            text = state.text.resolve(),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.text.default,
        )
    }
}

@Composable
private fun IngredientTone.colors(): AppColors.IngredientTone = with(AppTheme.colors.ingredient) {
    when (this@colors) {
        IngredientTone.ADDED_SUGAR -> addedSugar
        IngredientTone.SWEETENER -> sweetener
        IngredientTone.STARCH -> starch
        IngredientTone.MILK -> milk
        IngredientTone.ADDITIVE -> additive
        IngredientTone.PLAIN -> plain
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text = text,
        style = AppTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.text.default,
    )
}

@Preview(widthDp = 390, heightDp = 420)
@Composable
private fun IngredientSheetPreview() {
    ProductSheetPreview {
        IngredientSheetContent(state = IngredientSheetState.example(), onAction = {}, onClose = {})
    }
}

@Preview(widthDp = 360, heightDp = 570, fontScale = 2f)
@Composable
private fun IngredientSheetLargeFontPreview() {
    ProductSheetPreview {
        IngredientSheetContent(state = IngredientSheetState.example(), onAction = {}, onClose = {})
    }
}

@Preview(widthDp = 390, heightDp = 560)
@Composable
private fun IngredientSheetCompoundPreview() {
    ProductSheetPreview {
        IngredientSheetContent(state = IngredientSheetState.exampleCompound(), onAction = {}, onClose = {})
    }
}

@Preview(widthDp = 390, heightDp = 420)
@Composable
private fun IngredientSheetContentOpenPreview() {
    ProductSheetPreview {
        IngredientSheetContent(state = IngredientSheetState.exampleContent(), onAction = {}, onClose = {})
    }
}

@Preview(widthDp = 360, heightDp = 570, fontScale = 2f)
@Composable
private fun IngredientSheetContentOpenLargeFontPreview() {
    ProductSheetPreview {
        IngredientSheetContent(
            state = IngredientSheetState.exampleContent(
                compound = "Seasoning with sour cream, onion and a very long name",
            ),
            onAction = {},
            onClose = {},
        )
    }
}

@Preview(widthDp = 891, heightDp = 339)
@Composable
private fun IngredientSheetContentOpenLandscapePreview() {
    ProductSheetPreview {
        IngredientSheetContent(state = IngredientSheetState.exampleContent(), onAction = {}, onClose = {})
    }
}

@Preview(widthDp = 360, heightDp = 570)
@Composable
private fun IngredientSheetManyContentsPreview() {
    ProductSheetPreview {
        IngredientSheetContent(state = IngredientSheetState.exampleManyContents(), onAction = {}, onClose = {})
    }
}

@Preview(widthDp = 360, heightDp = 570, fontScale = 2f)
@Composable
private fun IngredientSheetManyContentsLargeFontPreview() {
    ProductSheetPreview {
        IngredientSheetContent(state = IngredientSheetState.exampleManyContents(), onAction = {}, onClose = {})
    }
}

@Preview(widthDp = 891, heightDp = 339)
@Composable
private fun IngredientSheetManyContentsLandscapePreview() {
    ProductSheetPreview {
        IngredientSheetContent(state = IngredientSheetState.exampleManyContents(), onAction = {}, onClose = {})
    }
}
