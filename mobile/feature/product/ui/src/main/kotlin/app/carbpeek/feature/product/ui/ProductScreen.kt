package app.carbpeek.feature.product.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.component.ContentCrossfade
import app.carbpeek.core.designsystem.component.PrimaryButton
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.component.Screen
import app.carbpeek.core.designsystem.component.Ui
import app.carbpeek.core.designsystem.component.bottomBarClearance
import app.carbpeek.core.designsystem.component.withBottomBarClearance
import app.carbpeek.core.designsystem.event.ObserveEvents
import app.carbpeek.core.designsystem.text.resolve
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.core.designsystem.theme.AppTheme
import app.carbpeek.feature.product.ui.component.Footer
import app.carbpeek.feature.product.ui.component.Header
import app.carbpeek.feature.product.ui.component.ImagePreview
import app.carbpeek.feature.product.ui.component.IngredientSheet
import app.carbpeek.feature.product.ui.component.IngredientsCard
import app.carbpeek.feature.product.ui.component.IngredientsInfoSheet
import app.carbpeek.feature.product.ui.component.MacrosCard
import app.carbpeek.feature.product.ui.component.ProductBlock
import app.carbpeek.feature.product.ui.component.RatingsCard
import app.carbpeek.feature.product.ui.component.example
import app.carbpeek.feature.product.ui.component.exampleFavourite
import app.carbpeek.feature.product.ui.component.exampleInsufficientData
import app.carbpeek.feature.product.ui.component.exampleShowProduct
import app.carbpeek.feature.product.ui.event.ProductAction
import app.carbpeek.feature.product.ui.event.ProductAction.OnRetryClicked
import app.carbpeek.feature.product.ui.event.ProductEvent.ProductNavigation
import app.carbpeek.feature.product.ui.state.FavouriteButtonState
import app.carbpeek.feature.product.ui.state.MacrosState
import app.carbpeek.feature.product.ui.state.ProductContentState.Error
import app.carbpeek.feature.product.ui.state.ProductContentState.Loading
import app.carbpeek.feature.product.ui.state.ProductContentState.ShowProduct
import app.carbpeek.feature.product.ui.state.ProductHeaderState
import app.carbpeek.feature.product.ui.state.ProductState
import app.carbpeek.feature.product.ui.state.VerdictState
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ProductScreen(onNavigation: (ProductNavigation) -> Unit) {
    val viewModel: ProductViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is ProductNavigation -> onNavigation(event)
        }
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: ProductState, onAction: (ProductAction) -> Unit) {
    Ui.Screen { innerPadding ->
        Ui.ContentCrossfade(
            targetState = state.content,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) { content ->
            when (content) {
                Loading -> CenteredContent(onAction) {
                    CircularProgressIndicator()
                }
                is Error -> CenteredContent(onAction) {
                    ErrorContent(content, onAction)
                }
                is ShowProduct -> ProductContent(content, onAction)
            }
        }
    }
}

@Composable
private fun CenteredContent(onAction: (ProductAction) -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomBarClearance()),
    ) {
        Header(favourite = null, onAction = onAction, modifier = Modifier.padding(AppTheme.spacing.l))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun ErrorContent(state: Error, onAction: (ProductAction) -> Unit) {
    Column(
        modifier = Modifier.padding(AppTheme.spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        Text(
            text = state.message.resolve(),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
        )
        Ui.PrimaryButton(text = stringResource(R.string.product_retry), onClick = { onAction(OnRetryClicked) })
    }
}

@Composable
private fun ProductContent(state: ShowProduct, onAction: (ProductAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(AppTheme.spacing.l).withBottomBarClearance()),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        Header(state.favourite, onAction)
        ProductBlock(state.header, onAction)
        MacrosCard(
            verdict = state.verdict,
            macros = state.macros,
            onAction = onAction,
        )
        state.scores?.let {
            RatingsCard(it)
        }
        state.ingredients?.let {
            IngredientsCard(it, onAction)
        }
        Footer(state.dataSource, onAction)
    }
    state.imagePreview?.let {
        ImagePreview(it, onAction)
    }
    state.ingredientSheet?.let {
        IngredientSheet(it, onAction)
    }
    state.ingredientsInfoSheet?.let {
        IngredientsInfoSheet(it, onAction)
    }
}

@Preview(showBackground = true, heightDp = 2200)
@Composable
private fun ProductShowRatedPreview() {
    AppTheme {
        ScreenContent(ProductState(exampleShowProduct())) {}
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ProductShowRatedPerServingPreview() {
    AppTheme {
        ScreenContent(
            ProductState(
                exampleShowProduct(
                    favourite = FavouriteButtonState.exampleFavourite,
                    macros = MacrosState.example(isPerServingSelected = true, highlight = Rating.Neutral),
                )
            )
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ProductShowInsufficientDataPreview() {
    AppTheme {
        ScreenContent(
            ProductState(
                exampleShowProduct(
                    header = ProductHeaderState.example(name = "Mystery bar"),
                    verdict = VerdictState.exampleInsufficientData(),
                    macros = MacrosState.example(isPerServingSelected = false, highlight = Rating.Unknown),
                )
            )
        ) {}
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ProductShowWithoutIngredientsPreview() {
    AppTheme {
        ScreenContent(ProductState(exampleShowProduct(ingredients = null))) {}
    }
}

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun ProductShowWithoutScoresPreview() {
    AppTheme {
        ScreenContent(ProductState(exampleShowProduct(scores = null))) {}
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 2600, fontScale = 1.5f)
@Composable
private fun ProductShowRatedLargeFontPreview() {
    AppTheme {
        ScreenContent(ProductState(exampleShowProduct())) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductLoadingPreview() {
    AppTheme {
        ScreenContent(ProductState(Loading)) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductErrorPreview() {
    AppTheme {
        ScreenContent(ProductState(Error(AppError.Network.toUiText()))) {}
    }
}
