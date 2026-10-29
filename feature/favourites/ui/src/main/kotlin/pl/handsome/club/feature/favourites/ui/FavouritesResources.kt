package pl.handsome.club.feature.favourites.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText

internal class FavouritesResources {

    fun productName(name: String): UiText =
        if (name.isBlank()) UiText.Res(R.string.favourites_unnamed_product) else UiText.Raw(name)

    fun brand(brand: String): UiText? = if (brand.isBlank()) null else UiText.Raw(brand)

    fun noResults(query: String): UiText = UiText.Res(R.string.favourites_no_results, UiArg.Text(query))

    fun loadError(error: AppError): UiText = error.toUiText()
}
