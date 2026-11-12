package pl.morning.tower.karbo.feature.product.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pl.morning.tower.karbo.core.designsystem.component.PrimaryButton
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.component.Ui
import pl.morning.tower.karbo.core.designsystem.component.VerdictBanner
import pl.morning.tower.karbo.core.designsystem.text.resolve
import pl.morning.tower.karbo.core.designsystem.theme.AppTheme
import pl.morning.tower.karbo.feature.product.ui.R
import pl.morning.tower.karbo.feature.product.ui.event.ProductAction
import pl.morning.tower.karbo.feature.product.ui.event.ProductAction.OnOpenFoodFactsClicked
import pl.morning.tower.karbo.feature.product.ui.state.VerdictState

@Composable
internal fun Verdict(state: VerdictState, onAction: (ProductAction) -> Unit) {
    when (state) {
        is VerdictState.Rated -> Ui.VerdictBanner(
            rating = state.rating,
            title = state.summary,
            details = state.reasons,
        )

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
