package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.state.ScoreState
import app.carbpeek.feature.product.ui.state.ScoreTone
import app.carbpeek.feature.product.ui.state.ScoresState

@Composable
internal fun RatingsCard(state: ScoresState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.colors.background.surface, shape = AppTheme.shapes.large)
            .border(
                width = AppTheme.borders.hairline,
                color = AppTheme.colors.border.default,
                shape = AppTheme.shapes.large,
            )
            .padding(AppTheme.spacing.m),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs)) {
            Text(
                text = stringResource(R.string.product_other_ratings),
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
            )
            Text(
                text = stringResource(R.string.product_other_ratings_note),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.muted,
            )
        }
        listOfNotNull(state.nutriScore, state.processing).forEach {
            ScoreRow(it)
        }
    }
}

@Composable
private fun ScoreRow(state: ScoreState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.iconTile)
                .background(color = state.tone.color(), shape = AppTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = state.badge.resolve(),
                style = AppTheme.typography.headline,
                color = AppTheme.colors.text.inverse,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.title.resolve(),
                style = AppTheme.typography.bodyStrong,
                color = AppTheme.colors.text.default,
            )
            Text(
                text = state.note.resolve(),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.text.muted,
            )
        }
    }
}

@Composable
private fun ScoreTone.color(): Color = with(AppTheme.colors.score) {
    when (this@color) {
        ScoreTone.NUTRI_SCORE_A -> nutriScoreA
        ScoreTone.NUTRI_SCORE_B -> nutriScoreB
        ScoreTone.NUTRI_SCORE_C -> nutriScoreC
        ScoreTone.NUTRI_SCORE_D -> nutriScoreD
        ScoreTone.NUTRI_SCORE_E -> nutriScoreE
        ScoreTone.PROCESSING -> processing
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingsCardPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            RatingsCard(ScoresState.example())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingsCardNutriScoreOnlyPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            RatingsCard(ScoresState.example(processing = null))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingsCardProcessingOnlyPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            RatingsCard(ScoresState.example(nutriScore = null))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, fontScale = 1.5f)
@Composable
private fun RatingsCardLargeFontPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            RatingsCard(
                ScoresState.example(
                    processing = ScoreState.exampleProcessing.copy(
                        note = UiText.Res(R.string.product_processing_2),
                    ),
                ),
            )
        }
    }
}
