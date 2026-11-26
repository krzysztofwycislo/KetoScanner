package app.carbpeek.feature.product.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.label
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.AnalyzedIngredient
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.IngredientName
import app.carbpeek.feature.analysis.api.model.RatingReason
import app.carbpeek.feature.analysis.api.model.Vitamin
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.product.api.model.NovaGroup
import app.carbpeek.feature.product.api.model.NutriScoreGrade

internal class ProductResources {

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.product_unnamed) else UiText.Raw(name)

    fun brand(brand: String): UiText? = if (brand.isBlank()) null else UiText.Raw(brand)

    fun imageDescription(): UiText = UiText.Res(R.string.product_image)

    fun categoryLabel(category: ProductCategory): UiText? =
        category.takeUnless { it == ProductCategory.OTHER }?.label

    fun imagePreviewDescription(): UiText = UiText.Res(R.string.product_image_preview)

    fun ratingSummary(dietRate: DietRate, mode: DietMode): UiText {
        val id = when (dietRate) {
            DietRate.GOOD -> when (mode) {
                DietMode.KETO -> R.string.product_verdict_good_keto
                DietMode.LOW_CARB -> R.string.product_verdict_good_low_carb
            }
            DietRate.NEUTRAL -> R.string.product_verdict_neutral
            DietRate.NOT_ADVISED -> when (mode) {
                DietMode.KETO -> R.string.product_verdict_not_advised_keto
                DietMode.LOW_CARB -> R.string.product_verdict_not_advised_low_carb
            }
        }
        return UiText.Res(id)
    }

    fun reason(reason: RatingReason): UiText = when (reason) {
        is RatingReason.HighCarbs ->
            UiText.Res(R.string.product_reason_high_carbs, UiArg.Numeric.Grams(reason.gramsPer100g))
        is RatingReason.HighCarbEnergyShare ->
            UiText.Res(R.string.product_reason_carb_energy_share, UiArg.Numeric.Percent(reason.share))
        is RatingReason.LowFatAndProteinToCarbRatio ->
            UiText.Res(R.string.product_reason_low_fat_and_protein_to_carb_ratio)
        is RatingReason.HighSugars ->
            UiText.Res(R.string.product_reason_high_sugars, UiArg.Numeric.Grams(reason.gramsPer100g))
        is RatingReason.HighCarbsPerServing -> UiText.Res(
            R.string.product_reason_high_carbs_per_serving,
            UiArg.Numeric.Grams(reason.carbsGrams),
            UiArg.Numeric.Grams(reason.servingGrams),
        )
    }

    fun carbsPer100g(grams: Double): UiText = UiText.Res(R.string.product_carbs_per_100g, UiArg.Numeric.Grams(grams))

    fun insufficientData(): UiText = UiText.Res(R.string.product_verdict_insufficient_data)

    fun insufficientDataDetail(): UiText = UiText.Res(R.string.product_verdict_insufficient_data_detail)

    fun insufficientDataAdvice(): UiText = UiText.Res(R.string.product_verdict_insufficient_data_advice)

    fun servingLabel(servingAmount: String?): UiText =
        if (servingAmount.isNullOrBlank()) UiText.Res(R.string.product_basis_serving)
        else UiText.Res(R.string.product_basis_serving_with_value, UiArg.Text(servingAmount))

    fun dataSource(barcode: String): UiText = UiText.Res(R.string.product_data_source, UiArg.Text(barcode))

    fun energy(): UiText = UiText.Res(R.string.product_energy)

    fun fats(): UiText = UiText.Res(R.string.product_fats)

    fun proteins(): UiText = UiText.Res(R.string.product_proteins)

    fun carbs(): UiText = UiText.Res(R.string.product_carbs)

    fun sugars(): UiText = UiText.Res(R.string.product_sugars)

    fun fibre(): UiText = UiText.Res(R.string.product_fibre)

    fun kcal(value: Double?): UiText =
        if (value == null) missingValue() else UiText.Res(R.string.product_value_kcal, UiArg.Numeric.Kcal(value))

    fun grams(value: Double?): UiText =
        if (value == null) missingValue() else UiText.Res(R.string.product_value_grams, UiArg.Numeric.Grams(value))

    fun ratingsTitle(hasDiet: Boolean): UiText =
        if (hasDiet) {
            UiText.Res(R.string.product_other_ratings)
        } else {
            UiText.Res(R.string.product_ratings)
        }

    fun ratingsNote(hasDiet: Boolean): UiText? =
        if (hasDiet) {
            UiText.Res(R.string.product_other_ratings_note)
        } else {
            null
        }

    fun nutriScoreBadge(grade: NutriScoreGrade): UiText = UiText.Raw(grade.name)

    fun nutriScoreTitle(): UiText = UiText.Res(R.string.product_nutri_score)

    fun nutriScoreNote(grade: NutriScoreGrade): UiText {
        val id = when (grade) {
            NutriScoreGrade.A -> R.string.product_nutri_score_a
            NutriScoreGrade.B -> R.string.product_nutri_score_b
            NutriScoreGrade.C -> R.string.product_nutri_score_c
            NutriScoreGrade.D -> R.string.product_nutri_score_d
            NutriScoreGrade.E -> R.string.product_nutri_score_e
        }
        return UiText.Res(id)
    }

    fun processingBadge(group: NovaGroup): UiText =
        UiText.Res(R.string.product_processing_badge, UiArg.Numeric.WholeNumber(group.number.toDouble()))

    fun processingTitle(): UiText = UiText.Res(R.string.product_processing)

    fun processingNote(group: NovaGroup): UiText {
        val id = when (group) {
            NovaGroup.UNPROCESSED -> R.string.product_processing_1
            NovaGroup.CULINARY_INGREDIENT -> R.string.product_processing_2
            NovaGroup.PROCESSED -> R.string.product_processing_3
            NovaGroup.ULTRA_PROCESSED -> R.string.product_processing_4
        }
        return UiText.Res(id)
    }

    fun ingredientName(name: IngredientName): UiText {
        name.vitamin?.let { return vitaminName(it) }
        val eNumber = name.eNumber ?: return UiText.Raw(name.name.upperCased())
        val knownName = name.knownENumberName()
        return when {
            knownName != null -> UiText.Res(
                R.string.product_ingredient_name_with_e_number,
                UiArg.Nested(knownName),
                UiArg.Text(eNumber),
            )
            name.hasCodeAsLabelName() -> UiText.Raw(eNumber)
            name.labelName.holdsCode(eNumber) -> UiText.Raw(name.labelName.upperCased())
            else -> UiText.Res(
                R.string.product_ingredient_name_with_e_number,
                UiArg.Text(name.labelName.upperCased()),
                UiArg.Text(eNumber),
            )
        }
    }

    fun ingredientRowLabel(ingredient: AnalyzedIngredient): UiText {
        val name = ingredientName(ingredient.name)
        val percent = ingredient.declaredPercent ?: return name
        return UiText.Res(
            R.string.product_ingredient_with_percent,
            UiArg.Nested(name),
            UiArg.Numeric.LabelPercent(percent / PERCENT),
        )
    }

    fun ingredientContentsCount(ingredient: AnalyzedIngredient): UiText? {
        val count = ingredient.contains.size
        return if (count == 0) {
            null
        } else {
            UiText.Plural(R.plurals.product_ingredient_inside, count, UiArg.Numeric.Count(count.toLong()))
        }
    }

    fun ingredientRowDescription(ingredient: AnalyzedIngredient): UiText =
        listOfNotNull(ingredientTag(ingredient.kind), ingredientContentsCount(ingredient))
            .fold(ingredientRowLabel(ingredient)) { description, detail ->
                UiText.Res(
                    R.string.product_ingredient_description,
                    UiArg.Nested(description),
                    UiArg.Nested(detail),
                )
            }

    fun ingredientTag(kind: IngredientKind): UiText? = when (kind) {
        IngredientKind.ADDED_SUGAR -> UiText.Res(R.string.product_ingredient_tag_added_sugar)
        IngredientKind.POLYOL,
        IngredientKind.SWEETENER,
        -> UiText.Res(R.string.product_ingredient_tag_sweetener)
        IngredientKind.STARCH -> UiText.Res(R.string.product_ingredient_tag_starch)
        IngredientKind.MILK -> UiText.Res(R.string.product_ingredient_tag_milk)
        IngredientKind.ADDITIVE -> UiText.Res(R.string.product_ingredient_tag_additive)
        IngredientKind.PLAIN -> null
    }

    fun ingredientDescription(ingredient: AnalyzedIngredient): IngredientDescription? =
        if (ingredient.name.vitamin == null) {
            descriptionsById[ingredient.id]
        } else {
            null
        }

    fun ingredientWhatHeading(): UiText = UiText.Res(R.string.product_ingredient_what)

    fun ingredientWhat(ingredient: AnalyzedIngredient): UiText? {
        val description = ingredientDescription(ingredient) ?: return kindWhat(ingredient.kind)
        return description.what?.let { UiText.Res(it) }
    }

    fun ingredientDoesHeading(): UiText = UiText.Res(R.string.product_ingredient_does)

    fun ingredientDoes(ingredient: AnalyzedIngredient): UiText? {
        val description = ingredientDescription(ingredient) ?: return kindDoes(ingredient.kind)
        return description.does?.let { UiText.Res(it) }
    }

    fun ingredientBackDescription(name: IngredientName): UiText =
        UiText.Res(R.string.product_ingredient_back, UiArg.Nested(ingredientName(name)))

    fun ingredientLabelName(name: IngredientName): UiText? {
        val isRenamed = name.vitamin != null || !name.name.equals(name.labelName, ignoreCase = true)
        val isReplacedInTitle = name.eNumber == null ||
            (name.knownENumberName() != null && !name.hasCodeAsLabelName())
        return if (isRenamed && isReplacedInTitle) {
            UiText.Res(R.string.product_ingredient_label_name, UiArg.Text(name.labelName))
        } else {
            null
        }
    }

    fun favouriteDescription(isFavourite: Boolean): UiText =
        if (isFavourite) UiText.Res(R.string.product_remove_from_favourites)
        else UiText.Res(R.string.product_add_to_favourites)

    fun addedToFavourites(): UiText = UiText.Res(R.string.product_added_to_favourites)

    fun removedFromFavourites(): UiText = UiText.Res(R.string.product_removed_from_favourites)

    fun error(error: AppError): UiText = error.toUiText()

    private fun missingValue(): UiText = UiText.Res(R.string.product_value_missing)

    private fun kindWhat(kind: IngredientKind): UiText? = when (kind) {
        IngredientKind.ADDED_SUGAR -> UiText.Res(R.string.product_ingredient_what_added_sugar)
        IngredientKind.POLYOL -> UiText.Res(R.string.product_ingredient_what_polyol)
        IngredientKind.SWEETENER -> UiText.Res(R.string.product_ingredient_what_sweetener)
        IngredientKind.STARCH -> UiText.Res(R.string.product_ingredient_what_starch)
        IngredientKind.MILK -> UiText.Res(R.string.product_ingredient_what_milk)
        IngredientKind.ADDITIVE -> UiText.Res(R.string.product_ingredient_what_additive)
        IngredientKind.PLAIN -> null
    }

    private fun kindDoes(kind: IngredientKind): UiText? = when (kind) {
        IngredientKind.POLYOL,
        IngredientKind.SWEETENER,
        -> UiText.Res(R.string.product_ingredient_does_sweetener)
        IngredientKind.ADDED_SUGAR,
        IngredientKind.STARCH,
        IngredientKind.MILK,
        IngredientKind.ADDITIVE,
        IngredientKind.PLAIN,
        -> null
    }

    private fun vitaminName(vitamin: Vitamin): UiText = when (vitamin) {
        Vitamin.RIBOFLAVIN -> UiText.Res(R.string.product_ingredient_vitamin_b2)
        Vitamin.NIACIN -> UiText.Res(R.string.product_ingredient_vitamin_b3)
    }

    private fun String.upperCased(): String = replaceFirstChar(Char::uppercase)

    private fun IngredientName.hasCodeAsLabelName(): Boolean = labelName.trim().matches(eNumberCodePattern)

    private fun String.holdsCode(eNumber: String): Boolean =
        filter(Char::isLetterOrDigit).contains(eNumber, ignoreCase = true)

    private fun IngredientName.knownENumberName(): UiText? = when (eNumber?.filter(Char::isDigit)) {
        "420" -> UiText.Res(R.string.product_ingredient_e420)
        "421" -> UiText.Res(R.string.product_ingredient_e421)
        "950" -> UiText.Res(R.string.product_ingredient_e950)
        "951" -> UiText.Res(R.string.product_ingredient_e951)
        "952" -> UiText.Res(R.string.product_ingredient_e952)
        "953" -> UiText.Res(R.string.product_ingredient_e953)
        "954" -> UiText.Res(R.string.product_ingredient_e954)
        "955" -> UiText.Res(R.string.product_ingredient_e955)
        "957" -> UiText.Res(R.string.product_ingredient_e957)
        "959" -> UiText.Res(R.string.product_ingredient_e959)
        "960" -> UiText.Res(R.string.product_ingredient_e960)
        "961" -> UiText.Res(R.string.product_ingredient_e961)
        "962" -> UiText.Res(R.string.product_ingredient_e962)
        "964" -> UiText.Res(R.string.product_ingredient_e964)
        "965" -> UiText.Res(R.string.product_ingredient_e965)
        "966" -> UiText.Res(R.string.product_ingredient_e966)
        "967" -> UiText.Res(R.string.product_ingredient_e967)
        "968" -> UiText.Res(R.string.product_ingredient_e968)
        "969" -> UiText.Res(R.string.product_ingredient_e969)
        else -> null
    }

    private companion object {
        const val PERCENT = 100.0
        val descriptionsById = ingredientDescriptions
            .flatMap { description -> description.ids.map { id -> id to description } }
            .toMap()
        val eNumberCodePattern = Regex("e[\\s-]*\\d{3,4}\\s*\\(?[a-z]*\\)?", RegexOption.IGNORE_CASE)
    }
}
