package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.state.ScoreTileState
import app.carbpeek.feature.product.ui.state.ScoreTone
import app.carbpeek.feature.product.ui.state.ScoresState

private const val SCORE_COLUMNS = 2

@Composable
internal fun ScoreTiles(state: ScoresState) {
    val tiles = listOfNotNull(state.nutriScore, state.processing)
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        tiles.forEach {
            ScoreTile(it, Modifier.weight(1f))
        }
        repeat(SCORE_COLUMNS - tiles.size) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ScoreTile(state: ScoreTileState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(AppTheme.colors.background.surface, AppTheme.shapes.card)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.card)
            .padding(AppTheme.spacing.s)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.iconTile)
                .background(state.tone.color(), AppTheme.shapes.medium),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = state.badge.resolve(),
                style = AppTheme.typography.headline,
                color = AppTheme.colors.text.inverse,
            )
        }
        Column {
            Text(
                text = state.title.resolve(),
                style = AppTheme.typography.bodyStrong,
                color = AppTheme.colors.text.default,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.note.resolve(),
                style = AppTheme.typography.caption,
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
private fun ScoreTilesPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            ScoreTiles(ScoresState.example())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScoreTilesNutriScoreOnlyPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            ScoreTiles(ScoresState.example(processing = null))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScoreTilesProcessingOnlyPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            ScoreTiles(ScoresState.example(nutriScore = null))
        }
    }
}
