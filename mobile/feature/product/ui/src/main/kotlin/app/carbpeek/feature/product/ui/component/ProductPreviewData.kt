package app.carbpeek.feature.product.ui.component

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.label
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.state.CategoryChipState
import app.carbpeek.feature.product.ui.state.FavouriteButtonState
import app.carbpeek.feature.product.ui.state.GlanceState
import app.carbpeek.feature.product.ui.state.GlanceTileState
import app.carbpeek.feature.product.ui.state.IngredientChipState
import app.carbpeek.feature.product.ui.state.IngredientDialogState
import app.carbpeek.feature.product.ui.state.IngredientLegendState
import app.carbpeek.feature.product.ui.state.IngredientSectionState
import app.carbpeek.feature.product.ui.state.IngredientTone
import app.carbpeek.feature.product.ui.state.IngredientsCardState
import app.carbpeek.feature.product.ui.state.IngredientsInfoDialogState
import app.carbpeek.feature.product.ui.state.ProductContentState.ShowProduct
import app.carbpeek.feature.product.ui.state.ProductHeaderState
import app.carbpeek.feature.product.ui.state.ScoreTileState
import app.carbpeek.feature.product.ui.state.ScoreTone
import app.carbpeek.feature.product.ui.state.ScoresState
import app.carbpeek.feature.product.ui.state.VerdictState
import kotlinx.collections.immutable.persistentListOf

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

internal val ScoreTileState.Companion.exampleNutriScore
    get() = ScoreTileState(
        badge = UiText.Raw("D"),
        title = UiText.Res(R.string.product_nutri_score),
        note = UiText.Res(R.string.product_nutri_score_d),
        tone = ScoreTone.NUTRI_SCORE_D,
    )

internal val ScoreTileState.Companion.exampleProcessing
    get() = ScoreTileState(
        badge = UiText.Res(R.string.product_processing_badge, UiArg.Numeric.WholeNumber(4.0)),
        title = UiText.Res(R.string.product_processing),
        note = UiText.Res(R.string.product_processing_4),
        tone = ScoreTone.PROCESSING,
    )

internal fun ScoresState.Companion.example(
    nutriScore: ScoreTileState? = ScoreTileState.exampleNutriScore,
    processing: ScoreTileState? = ScoreTileState.exampleProcessing,
) = ScoresState(
    nutriScore = nutriScore,
    processing = processing,
)

private fun chip(
    name: String,
    tone: IngredientTone = IngredientTone.PLAIN,
    tag: Int? = null,
    percent: Double? = null,
): IngredientChipState {
    val label = if (percent == null) {
        UiText.Raw(name)
    } else {
        UiText.Res(R.string.product_ingredient_with_percent, UiArg.Text(name), UiArg.Numeric.LabelPercent(percent))
    }
    val description = if (tag == null) {
        label
    } else {
        UiText.Res(
            R.string.product_ingredient_description,
            UiArg.Nested(label),
            UiArg.Nested(UiText.Res(tag)),
        )
    }
    return IngredientChipState(
        label = label,
        description = description,
        tone = tone,
    )
}

private fun legendEntry(tone: IngredientTone, label: Int) = IngredientLegendState(
    tone = tone,
    label = UiText.Res(label),
)

internal fun IngredientsInfoDialogState.Companion.example() = IngredientsInfoDialogState(
    legend = persistentListOf(
        legendEntry(tone = IngredientTone.ADDED_SUGAR, label = R.string.product_ingredient_tag_added_sugar),
        legendEntry(tone = IngredientTone.SWEETENER, label = R.string.product_ingredient_tag_sweetener),
        legendEntry(tone = IngredientTone.STARCH, label = R.string.product_ingredient_tag_starch),
        legendEntry(tone = IngredientTone.MILK, label = R.string.product_ingredient_tag_milk),
        legendEntry(tone = IngredientTone.ADDITIVE, label = R.string.product_ingredient_tag_additive),
    ),
)

internal fun IngredientsCardState.Companion.example() = IngredientsCardState(
    chips = persistentListOf(
        chip(name = "Peanuts", percent = 0.8),
        chip(
            name = "Sugar",
            tone = IngredientTone.ADDED_SUGAR,
            tag = R.string.product_ingredient_tag_added_sugar,
            percent = 0.087,
        ),
        chip(
            name = "Maltitol (E965)",
            tone = IngredientTone.SWEETENER,
            tag = R.string.product_ingredient_tag_sweetener,
        ),
        chip(name = "Wheat flour", tone = IngredientTone.STARCH, tag = R.string.product_ingredient_tag_starch),
        chip(name = "Whey powder", tone = IngredientTone.MILK, tag = R.string.product_ingredient_tag_milk),
        chip(
            name = "Lecithins (E322)",
            tone = IngredientTone.ADDITIVE,
            tag = R.string.product_ingredient_tag_additive,
        ),
        chip(name = "Seasoning with sour cream, onion and a name that wraps to two lines"),
    ),
)

internal fun IngredientDialogState.Companion.example() = IngredientDialogState(
    tag = UiText.Res(R.string.product_ingredient_tag_sweetener),
    tone = IngredientTone.SWEETENER,
    title = UiText.Res(
        R.string.product_ingredient_name_with_e_number,
        UiArg.Nested(UiText.Res(R.string.product_ingredient_e968)),
        UiArg.Text("E968"),
    ),
    labelName = UiText.Res(R.string.product_ingredient_label_name, UiArg.Text("eritritol")),
    contains = null,
    sections = persistentListOf(
        IngredientSectionState(
            heading = UiText.Res(R.string.product_ingredient_what),
            text = UiText.Res(R.string.product_ingredient_what_polyol),
        ),
        IngredientSectionState(
            heading = UiText.Res(R.string.product_ingredient_does),
            text = UiText.Res(R.string.product_ingredient_does_sweetener),
        ),
    ),
    noDescription = null,
)

internal fun IngredientDialogState.Companion.exampleCompound() = IngredientDialogState(
    tag = null,
    tone = IngredientTone.PLAIN,
    title = UiText.Raw("Mustard"),
    labelName = null,
    contains = UiText.Res(
        R.string.product_ingredient_contains,
        UiArg.Text("water, mustard seed, vinegar, salt and sugar"),
    ),
    sections = persistentListOf(),
    noDescription = UiText.Res(R.string.product_ingredient_no_description),
)

internal fun IngredientDialogState.Companion.exampleNoDescription() = IngredientDialogState(
    tag = null,
    tone = IngredientTone.PLAIN,
    title = UiText.Raw("Basmati rice"),
    labelName = UiText.Res(R.string.product_ingredient_label_name, UiArg.Text("riz basmati")),
    contains = null,
    sections = persistentListOf(),
    noDescription = UiText.Res(R.string.product_ingredient_no_description),
)

internal val exampleDataSource = UiText.Res(R.string.product_data_source, UiArg.Text("3017620422003"))

internal fun exampleShowProduct(
    header: ProductHeaderState = ProductHeaderState.example(),
    favourite: FavouriteButtonState? = FavouriteButtonState.exampleNotFavourite,
    verdict: VerdictState = VerdictState.exampleRated(),
    glance: GlanceState = GlanceState.example(isPerServingSelected = false, highlight = Rating.Neutral),
    scores: ScoresState? = ScoresState.example(),
    ingredients: IngredientsCardState? = IngredientsCardState.example(),
    ingredientDialog: IngredientDialogState? = null,
    ingredientsInfoDialog: IngredientsInfoDialogState? = null,
) = ShowProduct(
    header = header,
    favourite = favourite,
    verdict = verdict,
    glance = glance,
    scores = scores,
    ingredients = ingredients,
    imagePreview = null,
    ingredientDialog = ingredientDialog,
    ingredientsInfoDialog = ingredientsInfoDialog,
    dataSource = exampleDataSource,
)
