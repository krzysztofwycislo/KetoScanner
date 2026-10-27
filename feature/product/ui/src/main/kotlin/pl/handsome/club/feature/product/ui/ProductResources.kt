package pl.handsome.club.feature.product.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.feature.analysis.api.model.DietRate

internal class ProductResources {

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.product_unnamed) else UiText.Raw(name)

    fun brand(brand: String): UiText = UiText.Raw(brand)

    fun imageDescription(): UiText = UiText.Res(R.string.product_image)

    fun imagePreviewDescription(): UiText = UiText.Res(R.string.product_image_preview)

    fun ratingSummary(dietRate: DietRate): UiText = when (dietRate) {
        DietRate.GOOD -> UiText.Res(R.string.product_rating_good)
        DietRate.NEUTRAL -> UiText.Res(R.string.product_rating_neutral)
        DietRate.NOT_ADVISED -> UiText.Res(R.string.product_rating_not_advised)
    }

    fun ratingDetails(dietRate: DietRate, maxRateCarbAmount: Int): UiText {
        val id = when (dietRate) {
            DietRate.GOOD -> R.string.product_rating_good_details
            DietRate.NEUTRAL -> R.string.product_rating_neutral_details
            DietRate.NOT_ADVISED -> R.string.product_rating_not_advised_details
        }
        return UiText.Res(id, UiArg.Numeric.Count(maxRateCarbAmount.toLong()))
    }

    fun insufficientData(): UiText = UiText.Res(R.string.product_rating_insufficient_data)

    fun portion(maxProductPortion: Double, dailyCarbs: Int): UiText =
        UiText.Res(
            R.string.product_maximum_portion_details,
            UiArg.Numeric.WholeNumber(maxProductPortion),
            UiArg.Numeric.Count(dailyCarbs.toLong()),
        )

    fun perServingHeader(servingAmount: String?): UiText =
        if (servingAmount.isNullOrBlank()) UiText.Res(R.string.product_per_serving)
        else UiText.Res(R.string.product_per_serving_with_value, UiArg.Text(servingAmount))

    fun energy(): UiText = UiText.Res(R.string.product_energy)

    fun fats(): UiText = UiText.Res(R.string.product_fats)

    fun proteins(): UiText = UiText.Res(R.string.product_proteins)

    fun carbs(): UiText = UiText.Res(R.string.product_carbs)

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
