package pl.morning.tower.karbo.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.R
import pl.morning.tower.karbo.core.designsystem.icon.imageVector
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme

@Composable
fun Ui.RatingRow(
    rating: Rating,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val color = rating.color()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(rating.containerColor(), AppTheme.shapes.control)
            .padding(horizontal = AppTheme.spacing.s, vertical = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = rating.markIcon().imageVector,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(AppTheme.sizes.icon.s),
        )
        Text(
            text = title,
            style = AppTheme.typography.button,
            color = color,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = AppTheme.typography.bodyStrong,
            color = color,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingRowPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        ) {
            listOf(Rating.Positive, Rating.Neutral, Rating.Negative).forEach {
                Ui.RatingRow(
                    rating = it,
                    title = stringResource(R.string.designsystem_preview_rating),
                    value = stringResource(R.string.designsystem_preview_carbs, "6.7"),
                )
            }
        }
    }
}
