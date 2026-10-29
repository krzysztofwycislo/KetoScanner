package pl.handsome.club.feature.history.ui.state

import kotlinx.collections.immutable.toPersistentList
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.history.ui.HistoryResources
import pl.handsome.club.feature.history.ui.state.HistoryContentState.Empty
import pl.handsome.club.feature.history.ui.state.HistoryContentState.Error
import pl.handsome.club.feature.history.ui.state.HistoryContentState.InitialLoad
import pl.handsome.club.feature.history.ui.state.HistoryContentState.ShowItems
import pl.handsome.club.feature.history.ui.state.HistoryStatePrototype as Prototype

internal class HistoryStateFactory(private val resources: HistoryResources) {

    fun createFor(prototype: Prototype): HistoryState = HistoryState(
        subtitle = prototype.subtitle(),
        content = prototype.content(),
        isMenuOpen = prototype.isMenuOpen,
    )

    private fun Prototype.subtitle(): UiText? {
        val entries = entries
        return when {
            loadError != null -> null
            entries.isNullOrEmpty() -> null
            else -> resources.lastSearches(entries.size)
        }
    }

    private fun Prototype.content(): HistoryContentState {
        val entries = entries
        val loadError = loadError
        return when {
            loadError != null -> Error(resources.loadError(loadError))
            entries == null -> InitialLoad
            entries.isEmpty() -> Empty
            else -> ShowItems(entries.map { it.toItemState() }.toPersistentList())
        }
    }

    private fun ProductAnalysisHistoryEntry.toItemState(): HistoryItemState {
        val dietRate = macronutrientAnalysisResult?.carbsRate
        return HistoryItemState(
            id = id,
            barcode = productBarcode,
            name = resources.productName(productName),
            brand = resources.brand(productBrand),
            rating = dietRate.toRating(),
            ratingDescription = resources.rating(dietRate),
        )
    }

    private fun DietRate?.toRating(): Rating = when (this) {
        DietRate.GOOD -> Rating.Positive
        DietRate.NEUTRAL -> Rating.Neutral
        DietRate.NOT_ADVISED -> Rating.Negative
        null -> Rating.Unknown
    }
}
