package pl.morning.tower.karbo.feature.product.ui.component

import kotlinx.collections.immutable.persistentListOf
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.AppIcons
import pl.morning.tower.karbo.core.designsystem.text.UiArg
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.core.designsystem.text.label
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.product.ui.R
import pl.morning.tower.karbo.feature.product.ui.state.CategoryChipState
import pl.morning.tower.karbo.feature.product.ui.state.FavouriteButtonState
import pl.morning.tower.karbo.feature.product.ui.state.GlanceState
import pl.morning.tower.karbo.feature.product.ui.state.GlanceTileState
import pl.morning.tower.karbo.feature.product.ui.state.ProductContentState.ShowProduct
import pl.morning.tower.karbo.feature.product.ui.state.ProductHeaderState
import pl.morning.tower.karbo.feature.product.ui.state.VerdictState

internal fun ProductHeaderState.Companion.example(name: String = "Peanut butter") = ProductHeaderState(
    imageUrl = null,
    imageDescription = UiText.Res(R.string.product_image),
    isImageClickable = false,
    category = CategoryChipState(AppIcons.Jar, ProductCategory.SPREADS.label),
    brand = UiText.Raw("Nuts & Co"),
    name = UiText.Raw(name),
    nameMaxLines = 1,
)

internal val FavouriteButtonState.Companion.exampleNotFavourite
    get() = FavouriteButtonState(
        isFavourite = false,
        description = UiText.Res(R.string.product_add_to_favourites),
        isEnabled = true,
        icon = AppIcons.Heart,
    )

internal val FavouriteButtonState.Companion.exampleFavourite
    get() = FavouriteButtonState(
        isFavourite = true,
        description = UiText.Res(R.string.product_remove_from_favourites),
        isEnabled = true,
        icon = AppIcons.HeartFilled,
    )

internal fun VerdictState.Companion.exampleRated() = VerdictState.Rated(
    rating = Rating.Neutral,
    summary = UiText.Res(R.string.product_verdict_neutral),
    reasons = persistentListOf(
        UiText.Res(R.string.product_reason_carb_energy_share, UiArg.Numeric.Percent(0.19)),
        UiText.Res(R.string.product_reason_high_sugars, UiArg.Numeric.Grams(7.0)),
    ),
)

internal fun VerdictState.Companion.exampleInsufficientData() = VerdictState.InsufficientData(
    summary = UiText.Res(R.string.product_verdict_insufficient_data),
    detail = UiText.Res(R.string.product_verdict_insufficient_data_detail),
    advice = UiText.Res(R.string.product_verdict_insufficient_data_advice),
)

private fun grams(value: Double?): UiText =
    value?.let { UiText.Res(R.string.product_value_grams, UiArg.Numeric.Grams(it)) }
        ?: UiText.Res(R.string.product_value_missing)

private fun kcal(value: Double): UiText = UiText.Res(R.string.product_value_kcal, UiArg.Numeric.Kcal(value))

internal fun GlanceState.Companion.example(
    isPerServingSelected: Boolean,
    highlight: Rating,
    isServingToggleVisible: Boolean = true,
) = GlanceState(
    isServingToggleVisible = isServingToggleVisible,
    isPerServingSelected = isPerServingSelected,
    servingLabel = UiText.Res(R.string.product_basis_serving_with_value, UiArg.Text("15")),
    tiles = persistentListOf(
        GlanceTileState(
            label = UiText.Res(R.string.product_carbs),
            value = grams(if (isPerServingSelected) 2.0 else 13.0),
            highlight = highlight,
        ),
        GlanceTileState(UiText.Res(R.string.product_sugars), grams(if (isPerServingSelected) 1.1 else 7.0), null),
        GlanceTileState(UiText.Res(R.string.product_fibre), grams(if (isPerServingSelected) null else 11.0), null),
        GlanceTileState(UiText.Res(R.string.product_proteins), grams(if (isPerServingSelected) 3.8 else 25.0), null),
        GlanceTileState(UiText.Res(R.string.product_fats), grams(if (isPerServingSelected) 7.5 else 50.0), null),
        GlanceTileState(UiText.Res(R.string.product_energy), kcal(if (isPerServingSelected) 88.0 else 588.0), null),
    ),
)

internal val exampleDataSource = UiText.Res(R.string.product_data_source, UiArg.Text("3017620422003"))

internal fun exampleShowProduct(
    header: ProductHeaderState = ProductHeaderState.example(),
    favourite: FavouriteButtonState? = FavouriteButtonState.exampleNotFavourite,
    verdict: VerdictState = VerdictState.exampleRated(),
    glance: GlanceState = GlanceState.example(isPerServingSelected = false, highlight = Rating.Neutral),
) = ShowProduct(
    header = header,
    favourite = favourite,
    verdict = verdict,
    glance = glance,
    imagePreview = null,
    dataSource = exampleDataSource,
)
