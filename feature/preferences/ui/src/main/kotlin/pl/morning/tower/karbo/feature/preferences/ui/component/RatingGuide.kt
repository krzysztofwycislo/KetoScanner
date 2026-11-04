package pl.handsome.club.feature.preferences.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.component.RatingRow
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.preferences.ui.R
import pl.handsome.club.feature.preferences.ui.state.RatingGuideState

@Composable
internal fun RatingGuide(state: RatingGuideState) {
    val shape = AppTheme.shapes.large
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.background.surface, shape)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, shape)
            .padding(AppTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.preferences_rating_title),
            style = AppTheme.typography.section,
            color = AppTheme.colors.text.default,
        )
        Text(
            text = state.subtitle.resolve(),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.muted,
        )
        state.rows.forEach { row ->
            Ui.RatingRow(
                rating = row.rating,
                title = row.label.resolve(),
                value = row.value.resolve(),
            )
        }
        Text(
            text = state.footnote.resolve(),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colors.text.muted,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingGuidePreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            RatingGuide(previewRatingGuide(40))
        }
    }
}
