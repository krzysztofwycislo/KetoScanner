package pl.handsome.club.feature.product.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.CircleIconButton
import pl.handsome.club.core.designsystem.component.ContentCrossfade
import pl.handsome.club.core.designsystem.component.InfoChip
import pl.handsome.club.core.designsystem.component.PrimaryButton
import pl.handsome.club.core.designsystem.component.ProductImage
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.SegmentedToggle
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.VerdictBanner
import pl.handsome.club.core.designsystem.component.color
import pl.handsome.club.core.designsystem.component.containerColor
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.icon.imageVector
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.label
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.core.model.ProductCategory
import pl.handsome.club.feature.product.ui.event.ProductAction
import pl.handsome.club.feature.product.ui.event.ProductAction.OnBackClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnFavouriteClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnImageClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnImagePreviewDismissed
import pl.handsome.club.feature.product.ui.event.ProductAction.OnNameClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnOpenFoodFactsClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnPer100gClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnPerServingClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnRetryClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnWebPageUnavailable
import pl.handsome.club.feature.product.ui.event.ProductEvent.OpenWebPage
import pl.handsome.club.feature.product.ui.event.ProductEvent.ProductNavigation
import pl.handsome.club.feature.product.ui.state.CategoryChipState
import pl.handsome.club.feature.product.ui.state.FavouriteButtonState
import pl.handsome.club.feature.product.ui.state.GlanceState
import pl.handsome.club.feature.product.ui.state.GlanceTileState
import pl.handsome.club.feature.product.ui.state.ImagePreviewState
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
private fun Header(
    favourite: FavouriteButtonState?,
    onAction: (ProductAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ui.CircleIconButton(
            icon = AppIcons.Back.imageVector,
            contentDescription = UiText.Res(R.string.product_back),
            onClick = { onAction(OnBackClicked) },
        )
        if (favourite != null) {
            Ui.CircleIconButton(
                icon = (if (favourite.isFavourite) AppIcons.HeartFilled else AppIcons.Heart).imageVector,
                contentDescription = favourite.description,
                onClick = { onAction(OnFavouriteClicked) },
                enabled = favourite.isEnabled,
                contentColor = if (favourite.isFavourite) AppTheme.colors.favourite else AppTheme.colors.text.default,
            )
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

@Composable
private fun ProductBlock(header: ProductHeaderState, onAction: (ProductAction) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Ui.ProductImage(
            url = header.imageUrl,
            contentDescription = header.imageDescription,
            modifier = Modifier
                .size(AppTheme.sizes.productPhoto)
                .clip(AppTheme.shapes.large)
                .background(AppTheme.colors.background.surface)
                .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.large)
                .clickable(enabled = header.isImageClickable) { onAction(OnImageClicked) },
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        ) {
            header.category?.let {
                Ui.InfoChip(text = it.label.resolve(), icon = it.icon.imageVector)
            }
            Text(
                text = header.name.resolve(),
                style = AppTheme.typography.headline,
                color = AppTheme.colors.text.default,
                maxLines = header.nameMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onAction(OnNameClicked) },
            )
            header.brand?.let {
                Text(text = it.resolve(), style = AppTheme.typography.body, color = AppTheme.colors.text.muted)
            }
        }
    }
}

@Composable
private fun Verdict(state: VerdictState, onAction: (ProductAction) -> Unit) {
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

@Composable
private fun Glance(state: GlanceState, onAction: (ProductAction) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.product_at_a_glance),
                style = AppTheme.typography.section,
                color = AppTheme.colors.text.default,
                modifier = Modifier.weight(1f),
            )
            Ui.SegmentedToggle(
                options = listOf(stringResource(R.string.product_basis_100g), state.servingLabel.resolve()),
                selectedIndex = if (state.isPerServingSelected) 1 else 0,
                onSelect = { index -> onAction(if (index == 0) OnPer100gClicked else OnPerServingClicked) },
            )
        }
        state.tiles.chunked(GLANCE_COLUMNS).forEach { rowTiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                rowTiles.forEach {
                    GlanceTile(it, Modifier.weight(1f))
                }
                repeat(GLANCE_COLUMNS - rowTiles.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GlanceTile(state: GlanceTileState, modifier: Modifier = Modifier) {
    val highlight = state.highlight
    val tileModifier = if (highlight != null) {
        modifier.background(highlight.containerColor(), AppTheme.shapes.card)
    } else {
        modifier
            .background(AppTheme.colors.background.surface, AppTheme.shapes.card)
            .border(AppTheme.borders.hairline, AppTheme.colors.border.default, AppTheme.shapes.card)
    }
    val contentColor = highlight?.color() ?: AppTheme.colors.text.default
    Column(
        modifier = tileModifier.padding(AppTheme.spacing.s),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
    ) {
        Text(
            text = state.label.resolve(),
            style = AppTheme.typography.caption,
            color = highlight?.color() ?: AppTheme.colors.text.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = state.value.resolve(),
            style = AppTheme.typography.title,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun Footer(dataSource: UiText, onAction: (ProductAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dataSource.resolve(),
            style = AppTheme.typography.caption,
            color = AppTheme.colors.text.muted,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = AppTheme.sizes.touchTarget)
                .clip(AppTheme.shapes.small)
                .clickable(role = Role.Button) { onAction(OnOpenFoodFactsClicked) }
                .padding(horizontal = AppTheme.spacing.s),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.product_fix_on_open_food_facts),
                style = AppTheme.typography.label,
                color = AppTheme.colors.text.accent,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ImagePreview(state: ImagePreviewState, onAction: (ProductAction) -> Unit) {
    Dialog(onDismissRequest = { onAction(OnImagePreviewDismissed) }) {
        Ui.ProductImage(
            url = state.url,
            contentDescription = state.description,
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.background.default, AppTheme.shapes.small),
            contentScale = ContentScale.Fit,
        )
    }
}

private const val GLANCE_COLUMNS = 3

private fun ProductHeaderState.Companion.example(name: String = "Peanut butter") = ProductHeaderState(
    imageUrl = null,
    imageDescription = UiText.Res(R.string.product_image),
    isImageClickable = false,
    category = CategoryChipState(AppIcons.Jar, ProductCategory.SPREADS.label),
    brand = UiText.Raw("Nuts & Co"),
    name = UiText.Raw(name),
    nameMaxLines = 1,
)

private fun FavouriteButtonState.Companion.example(isFavourite: Boolean) = FavouriteButtonState(
    isFavourite = isFavourite,
    description = UiText.Res(
        if (isFavourite) R.string.product_remove_from_favourites else R.string.product_add_to_favourites
    ),
    isEnabled = true,
)

private fun VerdictState.Companion.exampleRated() = VerdictState.Rated(
    rating = Rating.Neutral,
    summary = UiText.Res(R.string.product_verdict_neutral),
    reasons = persistentListOf(
        UiText.Res(R.string.product_reason_carb_energy_share, UiArg.Numeric.Percent(0.19)),
        UiText.Res(R.string.product_reason_high_sugars, UiArg.Numeric.Grams(7.0)),
    ),
    portion = UiText.Res(
        R.string.product_advice_portion,
        UiArg.Numeric.WholeNumber(266.6),
        UiArg.Numeric.Count(40),
    ),
)

private fun VerdictState.Companion.exampleInsufficientData() = VerdictState.InsufficientData(
    summary = UiText.Res(R.string.product_verdict_insufficient_data),
    detail = UiText.Res(R.string.product_verdict_insufficient_data_detail),
    advice = UiText.Res(R.string.product_verdict_insufficient_data_advice),
)

private fun grams(value: Double?): UiText =
    value?.let { UiText.Res(R.string.product_value_grams, UiArg.Numeric.Grams(it)) }
        ?: UiText.Res(R.string.product_value_missing)

private fun kcal(value: Double): UiText = UiText.Res(R.string.product_value_kcal, UiArg.Numeric.Kcal(value))

private fun GlanceState.Companion.example(isPerServingSelected: Boolean, highlight: Rating) = GlanceState(
    isPerServingSelected = isPerServingSelected,
    servingLabel = UiText.Res(R.string.product_basis_serving_with_value, UiArg.Text("15")),
    tiles = persistentListOf(
        GlanceTileState(UiText.Res(R.string.product_carbs), grams(if (isPerServingSelected) 2.0 else 13.0), highlight),
        GlanceTileState(UiText.Res(R.string.product_sugars), grams(if (isPerServingSelected) 1.1 else 7.0), null),
        GlanceTileState(UiText.Res(R.string.product_fibre), grams(if (isPerServingSelected) null else 11.0), null),
        GlanceTileState(UiText.Res(R.string.product_proteins), grams(if (isPerServingSelected) 3.8 else 25.0), null),
        GlanceTileState(UiText.Res(R.string.product_fats), grams(if (isPerServingSelected) 7.5 else 50.0), null),
        GlanceTileState(UiText.Res(R.string.product_energy), kcal(if (isPerServingSelected) 88.0 else 588.0), null),
    ),
)

private fun exampleShowProduct(
    header: ProductHeaderState = ProductHeaderState.example(),
    favourite: FavouriteButtonState? = FavouriteButtonState.example(isFavourite = false),
    verdict: VerdictState = VerdictState.exampleRated(),
    glance: GlanceState = GlanceState.example(isPerServingSelected = false, highlight = Rating.Neutral),
) = ShowProduct(
    header = header,
    favourite = favourite,
    verdict = verdict,
    glance = glance,
    imagePreview = null,
    dataSource = UiText.Res(R.string.product_data_source, UiArg.Text("3017620422003")),
)

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
                    favourite = FavouriteButtonState.example(isFavourite = true),
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
