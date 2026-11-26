package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.icon.imageVector
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.state.IngredientRowState
import kotlinx.collections.immutable.PersistentList

@Composable
internal fun IngredientRows(
    rows: PersistentList<IngredientRowState>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        rows.forEachIndexed { index, row ->
            IngredientRowLine()
            IngredientRow(state = row, onClick = { onClick(index) })
        }
    }
}

@Composable
internal fun IngredientRowLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppTheme.borders.hairline)
            .background(AppTheme.colors.border.subtle),
    )
}

@Composable
private fun IngredientRow(state: IngredientRowState, onClick: () -> Unit) {
    val description = state.description.resolve()
    val interaction = if (state.isOpenable) {
        Modifier
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
    } else {
        Modifier.semantics(mergeDescendants = true) { contentDescription = description }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(interaction),
    ) {
        Row(
            modifier = Modifier
                .clearAndSetSemantics {}
                .fillMaxWidth()
                .heightIn(min = AppTheme.sizes.control.m)
                .padding(horizontal = AppTheme.spacing.m, vertical = AppTheme.spacing.s),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.label.resolve(),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.text.default,
                modifier = Modifier.weight(1f),
            )
            state.count?.let {
                Text(
                    text = it.resolve(),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.text.muted,
                    maxLines = 1,
                )
            }
            if (state.isOpenable) {
                Icon(
                    imageVector = AppIcons.ChevronRight.imageVector,
                    contentDescription = null,
                    tint = AppTheme.colors.text.subtle,
                    modifier = Modifier.size(AppTheme.sizes.chipArrow),
                )
            } else {
                Box(modifier = Modifier.size(AppTheme.sizes.chipArrow))
            }
        }
    }
}
