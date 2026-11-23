package app.carbpeek.feature.product.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.VerdictBanner
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnAddPhotosClicked
import app.carbpeek.feature.product.ui.state.VerdictState

@Composable
internal fun Verdict(state: VerdictState, onAction: (ProductAction) -> Unit) {
    when (state) {
        is VerdictState.Rated -> Ui.VerdictBanner(
            rating = state.rating,
            title = state.summary,
            details = state.reasons,
            shape = RectangleShape,
        )

        is VerdictState.InsufficientData -> Ui.VerdictBanner(
            rating = Rating.Unknown,
            title = state.summary,
            details = listOf(state.detail),
            shape = RectangleShape,
        ) {
            Text(text = state.advice.resolve(), style = AppTheme.typography.body)
            Ui.PrimaryButton(
                text = stringResource(R.string.product_add_photos),
                onClick = { onAction(OnAddPhotosClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
