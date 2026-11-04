package pl.handsome.club.feature.product.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.ContentCrossfade
import pl.handsome.club.core.designsystem.component.PrimaryButton
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.product.ui.component.Footer
import pl.handsome.club.feature.product.ui.component.Glance
import pl.handsome.club.feature.product.ui.component.Header
import pl.handsome.club.feature.product.ui.component.ImagePreview
import pl.handsome.club.feature.product.ui.component.ProductBlock
import pl.handsome.club.feature.product.ui.component.Verdict
import pl.handsome.club.feature.product.ui.component.example
import pl.handsome.club.feature.product.ui.component.exampleFavourite
import pl.handsome.club.feature.product.ui.component.exampleInsufficientData
import pl.handsome.club.feature.product.ui.component.exampleShowProduct
import pl.handsome.club.feature.product.ui.event.ProductAction
import pl.handsome.club.feature.product.ui.event.ProductAction.OnRetryClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnWebPageUnavailable
import pl.handsome.club.feature.product.ui.event.ProductEvent.OpenWebPage
import pl.handsome.club.feature.product.ui.event.ProductEvent.ProductNavigation
import pl.handsome.club.feature.product.ui.state.FavouriteButtonState
import pl.handsome.club.feature.product.ui.state.GlanceState
import pl.handsome.club.feature.product.ui.state.ProductContentState.Error
import pl.handsome.club.feature.product.ui.state.ProductContentState.Loading
import pl.handsome.club.feature.product.ui.state.ProductContentState.ShowProduct
import pl.handsome.club.feature.product.ui.state.ProductHeaderState
import pl.handsome.club.feature.product.ui.state.ProductState
import pl.handsome.club.feature.product.ui.state.VerdictState

@Composable
internal fun ProductScreen(onNavigation: (ProductNavigation) -> Unit) {
    val viewModel: ProductViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            is ProductNavigation -> onNavigation(event)
            is OpenWebPage -> try {
                uriHandler.openUri(event.url)
            } catch (_: IllegalArgumentException) {
                viewModel.onAction(OnWebPageUnavailable)
            }
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
    Column(modifier = Modifier.fillMaxSize()) {
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
            .padding(AppTheme.spacing.l),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
    ) {
        Header(state.favourite, onAction)
        ProductBlock(state.header, onAction)
        Verdict(state.verdict, onAction)
        Glance(state.glance, onAction)
        Footer(state.dataSource, onAction)
    }
    state.imagePreview?.let {
        ImagePreview(it, onAction)
    }
}

@Preview(showBackground = true, heightDp = 1400)
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
                    glance = GlanceState.example(isPerServingSelected = true, highlight = Rating.Neutral),
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
                    glance = GlanceState.example(isPerServingSelected = false, highlight = Rating.Unknown),
                )
            )
        ) {}
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
