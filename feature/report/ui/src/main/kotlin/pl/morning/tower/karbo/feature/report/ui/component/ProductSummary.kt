package pl.morning.tower.karbo.feature.report.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.component.IconTile
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.report.ui.state.ProductSummaryState

@Composable
internal fun ProductSummary(state: ProductSummaryState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background.surface, AppTheme.shapes.card)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.card)
            .padding(horizontal = AppTheme.spacing.s, vertical = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ui.IconTile(state.icon.imageVector)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.name.resolve(),
                style = AppTheme.typography.bodyStrong,
                color = AppTheme.colors.text.default,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (state.subtitle != null) {
                Text(
                    text = state.subtitle.resolve(),
                    style = AppTheme.typography.caption,
                    color = AppTheme.colors.text.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductSummaryPreview() {
    AppTheme {
        ProductSummary(
            state = ProductSummaryState.example(),
            modifier = Modifier.padding(AppTheme.spacing.m),
        )
    }
}
