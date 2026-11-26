package app.carbpeek.feature.product.ui.component

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcon
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.label
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.product.ui.R
import app.carbpeek.feature.product.ui.state.CategoryChipState
import app.carbpeek.feature.product.ui.state.FavouriteButtonState
import app.carbpeek.feature.product.ui.state.IngredientBackState
import app.carbpeek.feature.product.ui.state.IngredientRowState
import app.carbpeek.feature.product.ui.state.IngredientSectionState
import app.carbpeek.feature.product.ui.state.IngredientSheetState
import app.carbpeek.feature.product.ui.state.IngredientTone
import app.carbpeek.feature.product.ui.state.IngredientsCardState
import app.carbpeek.feature.product.ui.state.MacroValueState
import app.carbpeek.feature.product.ui.state.MacrosState
import app.carbpeek.feature.product.ui.state.ProductContentState.ShowProduct
import app.carbpeek.feature.product.ui.state.ProductHeaderState
import app.carbpeek.feature.product.ui.state.ScoreState
import app.carbpeek.feature.product.ui.state.ScoreTone
import app.carbpeek.feature.product.ui.state.ScoresState
import app.carbpeek.feature.product.ui.state.VerdictState
import kotlinx.collections.immutable.persistentListOf

internal fun ProductHeaderState.Companion.example(
    name: String = "Peanut butter",
    imageUrl: String? = null,
    placeholderIcon: AppIcon = AppIcons.Jar,
) = ProductHeaderState(
    imageUrl = imageUrl,
    imageDescription = UiText.Res(R.string.product_image),
    isImageClickable = imageUrl != null,
    placeholderIcon = placeholderIcon,
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

internal fun MacrosState.Companion.example(
    isPerServingSelected: Boolean,
    highlight: Rating?,
    isServingToggleVisible: Boolean = true,
) = MacrosState(
    isServingToggleVisible = isServingToggleVisible,
    isPerServingSelected = isPerServingSelected,
    servingLabel = UiText.Res(R.string.product_basis_serving_with_value, UiArg.Text("15")),
    values = persistentListOf(
        MacroValueState(
            label = UiText.Res(R.string.product_carbs),
            value = grams(if (isPerServingSelected) 2.0 else 13.0),
            highlight = highlight,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_sugars),
            value = grams(if (isPerServingSelected) 1.1 else 7.0),
            highlight = highlight,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_fibre),
            value = grams(if (isPerServingSelected) null else 11.0),
            highlight = null,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_proteins),
            value = grams(if (isPerServingSelected) 3.8 else 25.0),
            highlight = null,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_fats),
            value = grams(if (isPerServingSelected) 7.5 else 50.0),
            highlight = null,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_energy),
            value = kcal(if (isPerServingSelected) 88.0 else 588.0),
            highlight = null,
        ),
    ),
)

internal fun MacrosState.Companion.exampleWideValues() = MacrosState(
    isServingToggleVisible = true,
    isPerServingSelected = false,
    servingLabel = UiText.Res(R.string.product_basis_serving_with_value, UiArg.Text("15")),
    values = persistentListOf(
        MacroValueState(
            label = UiText.Res(R.string.product_carbs),
            value = grams(57.5),
            highlight = Rating.Neutral,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_sugars),
            value = grams(56.3),
            highlight = Rating.Neutral,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_fibre),
            value = grams(30.9),
            highlight = null,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_proteins),
            value = grams(100.0),
            highlight = null,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_fats),
            value = grams(100.0),
            highlight = null,
        ),
        MacroValueState(
            label = UiText.Res(R.string.product_energy),
            value = kcal(1035.0),
            highlight = null,
        ),
    ),
)

internal val ScoreState.Companion.exampleNutriScore
    get() = ScoreState(
        badge = UiText.Raw("D"),
        title = UiText.Res(R.string.product_nutri_score),
        note = UiText.Res(R.string.product_nutri_score_d),
        tone = ScoreTone.NUTRI_SCORE_D,
    )

internal val ScoreState.Companion.exampleProcessing
    get() = ScoreState(
        badge = UiText.Res(R.string.product_processing_badge, UiArg.Numeric.WholeNumber(4.0)),
        title = UiText.Res(R.string.product_processing),
        note = UiText.Res(R.string.product_processing_4),
        tone = ScoreTone.PROCESSING,
    )

internal fun ScoresState.Companion.example(
    nutriScore: ScoreState? = ScoreState.exampleNutriScore,
    processing: ScoreState? = ScoreState.exampleProcessing,
) = ScoresState(
    title = UiText.Res(R.string.product_other_ratings),
    note = UiText.Res(R.string.product_other_ratings_note),
    nutriScore = nutriScore,
    processing = processing,
)

internal fun ScoresState.Companion.exampleNoDiet() = example().copy(
    title = UiText.Res(R.string.product_ratings),
    note = null,
)

private fun row(
    name: String,
    tag: Int? = null,
    percent: Double? = null,
    count: Int? = null,
    isOpenable: Boolean = false,
): IngredientRowState {
    val label = if (percent == null) {
        UiText.Raw(name)
    } else {
        UiText.Res(R.string.product_ingredient_with_percent, UiArg.Text(name), UiArg.Numeric.LabelPercent(percent))
    }
    val countText = count?.let {
        UiText.Plural(R.plurals.product_ingredient_inside, it, UiArg.Numeric.Count(it.toLong()))
    }
    val description = listOfNotNull(tag?.let { UiText.Res(it) }, countText).fold(label) { description, detail ->
        UiText.Res(
            R.string.product_ingredient_description,
            UiArg.Nested(description),
            UiArg.Nested(detail),
        )
    }
    return IngredientRowState(
        label = label,
        count = countText,
        description = description,
        isOpenable = isOpenable,
    )
}

internal fun IngredientsCardState.Companion.example() = IngredientsCardState(
    rows = persistentListOf(
        row(name = "Peanuts", percent = 0.8),
        row(
            name = "Sugar",
            tag = R.string.product_ingredient_tag_added_sugar,
            percent = 0.087,
            isOpenable = true,
        ),
        row(
            name = "Maltitol (E965)",
            tag = R.string.product_ingredient_tag_sweetener,
            isOpenable = true,
        ),
        row(name = "Wheat flour", tag = R.string.product_ingredient_tag_starch),
        row(
            name = "Whey powder",
            tag = R.string.product_ingredient_tag_milk,
            isOpenable = true,
        ),
        row(
            name = "Lecithins (E322)",
            tag = R.string.product_ingredient_tag_additive,
            isOpenable = true,
        ),
        row(name = "Coating", percent = 0.34, count = 10, isOpenable = true),
        row(
            name = "Seasoning with sour cream, onion and a name that wraps to two lines",
            count = 12,
            isOpenable = true,
        ),
        row(name = "Partially hydrogenated vegetable fat from palm, coconut and shea", percent = 0.125),
    ),
)

internal fun IngredientSheetState.Companion.example() = IngredientSheetState(
    back = null,
    tag = UiText.Res(R.string.product_ingredient_tag_sweetener),
    tone = IngredientTone.SWEETENER,
    title = UiText.Res(
        R.string.product_ingredient_name_with_e_number,
        UiArg.Nested(UiText.Res(R.string.product_ingredient_e968)),
        UiArg.Text("E968"),
    ),
    labelName = UiText.Res(R.string.product_ingredient_label_name, UiArg.Text("eritritol")),
    contents = persistentListOf(),
    sections = persistentListOf(
        IngredientSectionState(
            heading = UiText.Res(R.string.product_ingredient_what),
            text = UiText.Res(R.string.product_ingredient_about_polyol_sugar_alcohol_made),
        ),
        IngredientSectionState(
            heading = UiText.Res(R.string.product_ingredient_does),
            text = UiText.Res(R.string.product_ingredient_about_adds_sweetness),
        ),
    ),
)

internal fun IngredientSheetState.Companion.exampleCompound() = IngredientSheetState(
    back = null,
    tag = null,
    tone = IngredientTone.PLAIN,
    title = UiText.Raw("Coating"),
    labelName = UiText.Res(R.string.product_ingredient_label_name, UiArg.Text("enrobage")),
    contents = persistentListOf(
        row(
            name = "Wheat flour",
            tag = R.string.product_ingredient_tag_starch,
            percent = 0.12,
        ),
        row(name = "Rapeseed oil"),
        row(name = "Water"),
        row(
            name = "Sugar",
            tag = R.string.product_ingredient_tag_added_sugar,
            isOpenable = true,
        ),
        row(
            name = "Whey powder",
            tag = R.string.product_ingredient_tag_milk,
            isOpenable = true,
        ),
        row(
            name = "Lecithins (E322)",
            tag = R.string.product_ingredient_tag_additive,
            isOpenable = true,
        ),
        row(name = "Salt"),
    ),
    sections = persistentListOf(),
)

internal fun IngredientSheetState.Companion.exampleContent(compound: String = "Coating") = IngredientSheetState(
    back = IngredientBackState(
        label = UiText.Raw(compound),
        description = UiText.Res(R.string.product_ingredient_back, UiArg.Text(compound)),
    ),
    tag = UiText.Res(R.string.product_ingredient_tag_milk),
    tone = IngredientTone.MILK,
    title = UiText.Raw("Whey powder"),
    labelName = UiText.Res(R.string.product_ingredient_label_name, UiArg.Text("lactosérum en poudre")),
    contents = persistentListOf(),
    sections = persistentListOf(
        IngredientSectionState(
            heading = UiText.Res(R.string.product_ingredient_what),
            text = UiText.Res(R.string.product_ingredient_about_dried_whey_liquid_left_when_milk_curdles_cheese),
        ),
    ),
)

internal fun IngredientSheetState.Companion.exampleManyContents() = exampleCompound().copy(
    contents = persistentListOf(
        row(name = "Wheat flour", tag = R.string.product_ingredient_tag_starch),
        row(name = "Rapeseed oil"),
        row(name = "Water"),
        row(name = "Partially hydrogenated vegetable fat from palm, coconut and shea", percent = 0.125),
        row(
            name = "Sugar",
            tag = R.string.product_ingredient_tag_added_sugar,
            isOpenable = true,
        ),
        row(
            name = "Glucose-fructose syrup",
            tag = R.string.product_ingredient_tag_added_sugar,
            isOpenable = true,
        ),
        row(
            name = "Whey powder",
            tag = R.string.product_ingredient_tag_milk,
            isOpenable = true,
        ),
        row(
            name = "Maltitol (E965)",
            tag = R.string.product_ingredient_tag_sweetener,
            isOpenable = true,
        ),
        row(
            name = "Lecithins (E322)",
            tag = R.string.product_ingredient_tag_additive,
            isOpenable = true,
        ),
        row(
            name = "Mono- and diglycerides of fatty acids (E471)",
            tag = R.string.product_ingredient_tag_additive,
            isOpenable = true,
        ),
        row(
            name = "Maize starch",
            tag = R.string.product_ingredient_tag_starch,
            isOpenable = true,
        ),
        row(name = "Wheat gluten", isOpenable = true),
        row(name = "Vitamin B2 (riboflavin)"),
        row(name = "Salt"),
        row(name = "Yeast", isOpenable = true),
        row(name = "Natural flavouring", isOpenable = true),
        row(name = "Black pepper"),
    ),
)

internal val exampleDataSource = UiText.Res(R.string.product_data_source, UiArg.Text("3017620422003"))

internal fun exampleShowProduct(
    header: ProductHeaderState = ProductHeaderState.example(),
    favourite: FavouriteButtonState? = FavouriteButtonState.exampleNotFavourite,
    verdict: VerdictState? = VerdictState.exampleRated(),
    macros: MacrosState = MacrosState.example(isPerServingSelected = false, highlight = Rating.Neutral),
    scores: ScoresState? = ScoresState.example(),
    ingredients: IngredientsCardState? = IngredientsCardState.example(),
) = ShowProduct(
    header = header,
    favourite = favourite,
    verdict = verdict,
    macros = macros,
    scores = scores,
    ingredients = ingredients,
    imagePreview = null,
    ingredientSheet = null,
    ingredientsInfoSheet = null,
    dataSource = exampleDataSource,
)
