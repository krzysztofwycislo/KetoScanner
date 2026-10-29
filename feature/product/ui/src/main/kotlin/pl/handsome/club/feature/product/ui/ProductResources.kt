package pl.handsome.club.feature.product.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.RatingReason
import pl.handsome.club.feature.preferences.api.model.DietMode

internal class ProductResources {

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.product_unnamed) else UiText.Raw(name)

    fun brand(brand: String): UiText = UiText.Raw(brand)

    fun imageDescription(): UiText = UiText.Res(R.string.product_image)

    fun imagePreviewDescription(): UiText = UiText.Res(R.string.product_image_preview)

    fun ratingSummary(dietRate: DietRate, mode: DietMode): UiText {
        val id = when (dietRate) {
            DietRate.GOOD -> R.string.product_rating_good
            DietRate.NEUTRAL -> R.string.product_rating_neutral
            DietRate.NOT_ADVISED -> R.string.product_rating_not_advised
        }
        return UiText.Res(id, UiArg.Nested(dietModeName(mode)))
    }

    fun reason(reason: RatingReason): UiText = when (reason) {
        is RatingReason.HighCarbs ->
            UiText.Res(R.string.product_reason_high_carbs, UiArg.Numeric.Grams(reason.gramsPer100g))
        is RatingReason.HighCarbEnergyShare ->
            UiText.Res(R.string.product_reason_carb_energy_share, UiArg.Numeric.Percent(reason.share))
        is RatingReason.LowFatAndProteinToCarbRatio ->
            UiText.Res(R.string.product_reason_low_fat_and_protein_to_carb_ratio, UiArg.Numeric.Ratio(reason.ratio))
        is RatingReason.HighSugars ->
            UiText.Res(R.string.product_reason_high_sugars, UiArg.Numeric.Grams(reason.gramsPer100g))
        is RatingReason.HighCarbsPerServing -> UiText.Res(
            R.string.product_reason_high_carbs_per_serving,
            UiArg.Numeric.Grams(reason.carbsGrams),
            UiArg.Numeric.Grams(reason.servingGrams),
        )
    }

    fun carbsPer100g(grams: Double): UiText = UiText.Res(R.string.product_carbs_per_100g, UiArg.Numeric.Grams(grams))

    fun insufficientData(): UiText = UiText.Res(R.string.product_rating_insufficient_data)

    fun portion(maxProductPortion: Double?, dailyCarbs: Int): UiText =
        if (maxProductPortion == null) UiText.Res(R.string.product_maximum_portion_unlimited)
        else UiText.Res(
            R.string.product_maximum_portion_details,
            UiArg.Numeric.WholeNumber(maxProductPortion),
            UiArg.Numeric.Count(dailyCarbs.toLong()),
        )

    private fun dietModeName(mode: DietMode): UiText = when (mode) {
        DietMode.KETO -> UiText.Res(R.string.product_diet_mode_keto)
        DietMode.LOW_CARB -> UiText.Res(R.string.product_diet_mode_low_carb)
    }

    fun perServingHeader(servingAmount: String?): UiText =
        if (servingAmount.isNullOrBlank()) UiText.Res(R.string.product_per_serving)
        else UiText.Res(R.string.product_per_serving_with_value, UiArg.Text(servingAmount))

    fun energy(): UiText = UiText.Res(R.string.product_energy)

    fun fats(): UiText = UiText.Res(R.string.product_fats)

    fun proteins(): UiText = UiText.Res(R.string.product_proteins)

    fun carbs(): UiText = UiText.Res(R.string.product_carbs)

    fun sugars(): UiText = UiText.Res(R.string.product_sugars)

    fun fibre(): UiText = UiText.Res(R.string.product_fibre)

    fun salt(): UiText = UiText.Res(R.string.product_salt)

    fun kcal(value: Double?): UiText =
        if (value == null) missingValue() else UiText.Res(R.string.product_value_kcal, UiArg.Numeric.Kcal(value))

    fun grams(value: Double?): UiText =
        if (value == null) missingValue() else UiText.Res(R.string.product_value_grams, UiArg.Numeric.Grams(value))

    fun favouriteDescription(isFavourite: Boolean): UiText =
        if (isFavourite) UiText.Res(R.string.product_remove_from_favourites)
        else UiText.Res(R.string.product_add_to_favourites)

    fun addedToFavourites(): UiText = UiText.Res(R.string.product_added_to_favourites)

    fun removedFromFavourites(): UiText = UiText.Res(R.string.product_removed_from_favourites)

    fun error(error: AppError): UiText = error.toUiText()

    private fun missingValue(): UiText = UiText.Res(R.string.product_value_missing)
}
