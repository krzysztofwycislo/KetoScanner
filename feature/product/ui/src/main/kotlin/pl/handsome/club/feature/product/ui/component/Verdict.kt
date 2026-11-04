package pl.handsome.club.feature.product.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.handsome.club.core.designsystem.component.PrimaryButton
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.VerdictBanner
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.product.ui.R
import pl.handsome.club.feature.product.ui.event.ProductAction
import pl.handsome.club.feature.product.ui.event.ProductAction.OnOpenFoodFactsClicked
import pl.handsome.club.feature.product.ui.state.VerdictState

@Composable
internal fun Verdict(state: VerdictState, onAction: (ProductAction) -> Unit) {
    when (state) {
        is VerdictState.Rated -> Ui.VerdictBanner(
            rating = state.rating,
            title = state.summary,
            details = state.reasons,
        ) {
            Text(text = state.portion.resolve(), style = AppTheme.typography.body)
        }

        is VerdictState.InsufficientData -> Ui.VerdictBanner(
            rating = Rating.Unknown,
            title = state.summary,
            details = listOf(state.detail),
        ) {
            Text(text = state.advice.resolve(), style = AppTheme.typography.body)
            Ui.PrimaryButton(
                text = stringResource(R.string.product_add_to_open_food_facts),
                onClick = { onAction(OnOpenFoodFactsClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VerdictRatedPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            Verdict(VerdictState.exampleRated()) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VerdictInsufficientDataPreview() {
    AppTheme {
        Box(modifier = Modifier.padding(AppTheme.spacing.l)) {
            Verdict(VerdictState.exampleInsufficientData()) {}
        }
    }
}
