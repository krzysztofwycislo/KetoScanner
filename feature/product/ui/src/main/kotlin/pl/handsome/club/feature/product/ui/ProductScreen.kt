package pl.handsome.club.feature.product.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.ContentCrossfade
import pl.handsome.club.core.designsystem.component.PortionIcon
import pl.handsome.club.core.designsystem.component.ProductImage
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.component.RatingIcon
import pl.handsome.club.core.designsystem.component.Screen
import pl.handsome.club.core.designsystem.component.Ui
import pl.handsome.club.core.designsystem.component.color
import pl.handsome.club.core.designsystem.event.ObserveEvents
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.resolve
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.designsystem.theme.AppTheme
import pl.handsome.club.feature.product.ui.event.ProductAction
import pl.handsome.club.feature.product.ui.event.ProductAction.OnBackClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnFavouriteClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnImageClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnImagePreviewDismissed
import pl.handsome.club.feature.product.ui.event.ProductAction.OnNameClicked
import pl.handsome.club.feature.product.ui.event.ProductAction.OnRetryClicked
import pl.handsome.club.feature.product.ui.event.ProductEvent.ProductNavigation
import pl.handsome.club.feature.product.ui.state.FavouriteButtonState
import pl.handsome.club.feature.product.ui.state.ImagePreviewState
import pl.handsome.club.feature.product.ui.state.NutrientRowState
import pl.handsome.club.feature.product.ui.state.NutrientTableState
import pl.handsome.club.feature.product.ui.state.ProductContentState.Error
import pl.handsome.club.feature.product.ui.state.ProductContentState.Loading
import pl.handsome.club.feature.product.ui.state.ProductContentState.ShowProduct
import pl.handsome.club.feature.product.ui.state.ProductHeaderState
import pl.handsome.club.feature.product.ui.state.ProductState
import pl.handsome.club.feature.product.ui.state.VerdictState

@Composable
fun ProductScreen(onNavigation: (ProductNavigation) -> Unit) {
    val viewModel: ProductViewModel = koinViewModel()

    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        if (event is ProductNavigation) onNavigation(event)
    }

    ScreenContent(state, viewModel::onAction)
}

@Composable
private fun ScreenContent(state: ProductState, onAction: (ProductAction) -> Unit) {
    Ui.Screen { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Ui.ContentCrossfade(targetState = state.content, modifier = Modifier.fillMaxSize()) { content ->
                when (content) {
                    Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                    is Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorContent(content, onAction)
                    }

                    is ShowProduct -> ProductContent(content, onAction)
                }
            }
            BackButton(onAction, Modifier.align(Alignment.TopStart))
        }
    }
}

@Composable
private fun BackButton(onAction: (ProductAction) -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = { onAction(OnBackClicked) },
        modifier = modifier.padding(AppTheme.spacing.xs),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = AppTheme.colors.background.scrim,
            contentColor = AppTheme.colors.text.inverse,
        ),
    ) {
        Icon(
            imageVector = AppIcons.Back,
            contentDescription = stringResource(R.string.product_back),
            modifier = Modifier.size(AppTheme.sizes.iconS),
        )
    }
}

@Composable
private fun ErrorContent(state: Error, onAction: (ProductAction) -> Unit) {
    Column(
        modifier = Modifier.padding(AppTheme.spacing.m),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
    ) {
        Text(
            text = state.message.resolve(),
            style = AppTheme.typography.body,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = { onAction(OnRetryClicked) }) {
            Text(text = stringResource(R.string.product_retry), style = AppTheme.typography.body)
        }
    }
}

@Composable
private fun ProductContent(state: ShowProduct, onAction: (ProductAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Ui.ProductImage(
            url = state.header.imageUrl,
            contentDescription = state.header.imageDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(AppTheme.sizes.productImageHeight)
                .clickable(enabled = state.header.isImageClickable) { onAction(OnImageClicked) },
        )
        Title(state.header, state.favourite, onAction)
        Column(
            modifier = Modifier.padding(AppTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.l),
        ) {
            Verdict(state.verdict)
            NutrientTable(state.nutrients)
        }
    }
    state.imagePreview?.let { ImagePreview(it, onAction) }
}

@Composable
private fun Title(header: ProductHeaderState, favourite: FavouriteButtonState?, onAction: (ProductAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppTheme.spacing.m, end = AppTheme.spacing.m, top = AppTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        ) {
            Text(
                text = header.brand.resolve(),
                style = AppTheme.typography.caption,
                color = AppTheme.colors.text.default,
            )
            Text(
                text = header.name.resolve(),
                style = AppTheme.typography.title,
                color = AppTheme.colors.text.default,
                maxLines = header.nameMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onAction(OnNameClicked) },
            )
        }
        favourite?.let { FavouriteButton(it, onAction) }
    }
}

@Composable
private fun FavouriteButton(state: FavouriteButtonState, onAction: (ProductAction) -> Unit) {
    IconButton(
        onClick = { onAction(OnFavouriteClicked) },
        enabled = state.isEnabled,
        modifier = Modifier
            .padding(start = AppTheme.spacing.xs)
            .size(AppTheme.sizes.thumbnail),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = AppTheme.colors.text.default,
            contentColor = AppTheme.colors.text.inverse,
            disabledContainerColor = AppTheme.colors.text.default,
        ),
    ) {
        Icon(
            imageVector = if (state.isFavourite) AppIcons.Favourite else AppIcons.FavouriteBorder,
            contentDescription = state.description.resolve(),
            modifier = Modifier.size(AppTheme.sizes.iconM),
        )
    }
}

@Composable
private fun Verdict(state: VerdictState) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.s)) {
        when (state) {
            is VerdictState.Rated -> {
                VerdictRow(
                    icon = { Ui.RatingIcon(rating = state.rating, contentDescription = state.summary) },
                    title = state.summary.resolve(),
                    details = state.details.resolve(),
                    color = state.rating.color(),
                )
                VerdictRow(
                    icon = {
                        Ui.PortionIcon(
                            rating = state.rating,
                            contentDescription = UiText.Res(R.string.product_maximum_portion),
                        )
                    },
                    title = stringResource(R.string.product_maximum_portion),
                    details = state.portion.resolve(),
                    color = state.rating.color(),
                )
            }

            is VerdictState.InsufficientData -> VerdictRow(
                icon = { Ui.RatingIcon(rating = Rating.Unknown, contentDescription = state.summary) },
                title = state.summary.resolve(),
                details = null,
                color = Rating.Unknown.color(),
            )
        }
    }
}

@Composable
private fun VerdictRow(
    icon: @Composable () -> Unit,
    title: String,
    details: String?,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Column(modifier = Modifier.padding(start = AppTheme.spacing.s)) {
            Text(text = title, style = AppTheme.typography.body, color = color)
            details?.let {
                Text(text = it, style = AppTheme.typography.body, color = AppTheme.colors.text.default)
            }
        }
    }
}

@Composable
private fun NutrientTable(state: NutrientTableState) {
    Column {
        NutrientRow(
            label = stringResource(R.string.product_macros),
            perServing = state.perServingHeader.resolve(),
            per100g = stringResource(R.string.product_per_100g),
            isHeader = true,
        )
        state.rows.forEach { row ->
            HorizontalDivider(thickness = AppTheme.borders.hairline, color = AppTheme.colors.border.default)
            NutrientRow(
                label = row.label.resolve(),
                perServing = row.perServing.resolve(),
                per100g = row.per100g.resolve(),
                isHeader = false,
            )
        }
    }
}

@Composable
private fun NutrientRow(label: String, perServing: String, per100g: String, isHeader: Boolean) {
    val style = if (isHeader) AppTheme.typography.title else AppTheme.typography.body
    val valueStyle = if (isHeader) AppTheme.typography.caption else AppTheme.typography.body
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = style,
            color = AppTheme.colors.text.default,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = perServing,
            style = valueStyle,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = per100g,
            style = valueStyle,
            color = AppTheme.colors.text.default,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
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

private fun ProductHeaderState.Companion.example(name: String = "Peanut butter") = ProductHeaderState(
    imageUrl = null,
    imageDescription = UiText.Res(R.string.product_image),
    isImageClickable = false,
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
    rating = Rating.Positive,
    summary = UiText.Res(R.string.product_rating_good),
    details = UiText.Res(R.string.product_rating_good_details, UiArg.Numeric.Count(13)),
    portion = UiText.Res(
        R.string.product_maximum_portion_details,
        UiArg.Numeric.WholeNumber(266.6),
        UiArg.Numeric.Count(40),
    ),
)

private fun NutrientRowState.Companion.example(label: Int, perServing: Double?, per100g: Double?) = NutrientRowState(
    label = UiText.Res(label),
    perServing = perServing?.let { UiText.Res(R.string.product_value_grams, UiArg.Numeric.Grams(it)) }
        ?: UiText.Res(R.string.product_value_missing),
    per100g = per100g?.let { UiText.Res(R.string.product_value_grams, UiArg.Numeric.Grams(it)) }
        ?: UiText.Res(R.string.product_value_missing),
)

private fun NutrientTableState.Companion.example() = NutrientTableState(
    perServingHeader = UiText.Res(R.string.product_per_serving_with_value, UiArg.Text("15")),
    rows = persistentListOf(
        NutrientRowState(
            UiText.Res(R.string.product_energy),
            UiText.Res(R.string.product_value_kcal, UiArg.Numeric.Kcal(88.0)),
            UiText.Res(R.string.product_value_kcal, UiArg.Numeric.Kcal(588.0)),
        ),
        NutrientRowState.example(R.string.product_fats, 7.5, 50.0),
        NutrientRowState.example(R.string.product_proteins, 3.8, 25.0),
        NutrientRowState.example(R.string.product_carbs, 2.0, 13.0),
        NutrientRowState.example(R.string.product_salt, null, 1.1),
    ),
)

@Preview(showBackground = true)
@Composable
private fun ProductShowRatedPreview() {
    AppTheme {
        ScreenContent(
            ProductState(
                ShowProduct(
                    header = ProductHeaderState.example(),
                    favourite = FavouriteButtonState.example(isFavourite = true),
                    verdict = VerdictState.exampleRated(),
                    nutrients = NutrientTableState.example(),
                    imagePreview = null,
                )
            )
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductShowInsufficientDataPreview() {
    AppTheme {
        ScreenContent(
            ProductState(
                ShowProduct(
                    header = ProductHeaderState.example(name = "Mystery bar"),
                    favourite = FavouriteButtonState.example(isFavourite = false),
                    verdict = VerdictState.InsufficientData(UiText.Res(R.string.product_rating_insufficient_data)),
                    nutrients = NutrientTableState.example(),
                    imagePreview = null,
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
