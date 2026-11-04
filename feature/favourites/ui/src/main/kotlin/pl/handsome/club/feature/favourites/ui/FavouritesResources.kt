package pl.handsome.club.feature.favourites.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.preferences.api.model.DietMode

internal class FavouritesResources {

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.favourites_unnamed_product) else UiText.Raw(name)

    fun brand(brand: String): UiText? = if (brand.isBlank()) null else UiText.Raw(brand)

    fun noResults(query: String): UiText = UiText.Res(R.string.favourites_no_results, UiArg.Text(query))

    fun carbs(grams: Double?): UiText =
        if (grams == null) UiText.Res(R.string.favourites_carbs_missing)
        else UiText.Res(R.string.favourites_carbs, UiArg.Numeric.Grams(grams))

    fun verdictDescription(rate: DietRate?, mode: DietMode, carbs: Double?): UiText {
        val verdict = verdict(rate, mode)
        return if (carbs == null) verdict
        else UiText.Res(
            R.string.favourites_verdict_with_carbs,
            UiArg.Nested(verdict),
            UiArg.Nested(UiText.Res(R.string.favourites_carbs_per_100g, UiArg.Numeric.Grams(carbs))),
        )
    }

    fun loadError(error: AppError): UiText = error.toUiText()

    fun groupCount(count: Int): UiText =
        UiText.Res(R.string.favourites_group_count, UiArg.Numeric.Count(count.toLong()))

    fun groupToggleState(expanded: Boolean): UiText =
        UiText.Res(if (expanded) R.string.favourites_group_expanded else R.string.favourites_group_collapsed)

    private fun verdict(rate: DietRate?, mode: DietMode): UiText = when (rate) {
        DietRate.GOOD -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.favourites_verdict_good_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.favourites_verdict_good_low_carb)
        }
        DietRate.NEUTRAL -> UiText.Res(R.string.favourites_verdict_neutral)
        DietRate.NOT_ADVISED -> when (mode) {
            DietMode.KETO -> UiText.Res(R.string.favourites_verdict_not_advised_keto)
            DietMode.LOW_CARB -> UiText.Res(R.string.favourites_verdict_not_advised_low_carb)
        }
        null -> UiText.Res(R.string.favourites_verdict_unknown)
    }
}
