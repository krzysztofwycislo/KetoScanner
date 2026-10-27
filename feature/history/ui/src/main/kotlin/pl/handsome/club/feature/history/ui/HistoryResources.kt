package pl.handsome.club.feature.history.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.feature.analysis.api.model.DietRate

internal class HistoryResources {

    fun lastSearches(count: Int): UiText =
        UiText.Plural(R.plurals.history_last_searches, count, UiArg.Numeric.Count(count.toLong()))

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.history_unnamed_product) else UiText.Raw(name)

    fun brand(brand: String): UiText = UiText.Raw(brand)

    fun rating(dietRate: DietRate?): UiText = when (dietRate) {
        DietRate.GOOD -> UiText.Res(R.string.history_rating_good)
        DietRate.NEUTRAL -> UiText.Res(R.string.history_rating_neutral)
        DietRate.NOT_ADVISED -> UiText.Res(R.string.history_rating_not_advised)
        null -> UiText.Res(R.string.history_rating_unknown)
    }

    fun loadError(error: AppError): UiText = error.toUiText()
}
