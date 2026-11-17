package app.carbpeek.feature.product.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.label
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.RatingReason
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

    fun favouriteDescription(isFavourite: Boolean): UiText =
        if (isFavourite) UiText.Res(R.string.product_remove_from_favourites)
        else UiText.Res(R.string.product_add_to_favourites)

    fun addedToFavourites(): UiText = UiText.Res(R.string.product_added_to_favourites)

    fun removedFromFavourites(): UiText = UiText.Res(R.string.product_removed_from_favourites)

    fun error(error: AppError): UiText = error.toUiText()

    private fun missingValue(): UiText = UiText.Res(R.string.product_value_missing)
}
