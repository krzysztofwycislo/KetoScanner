package app.carbpeek.feature.home.ui.state

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.icon
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.home.ui.HomeResources
import app.carbpeek.feature.home.ui.state.HomeRecentState.Empty
import app.carbpeek.feature.home.ui.state.HomeRecentState.Error
import app.carbpeek.feature.home.ui.state.HomeRecentState.InitialLoad
import app.carbpeek.feature.home.ui.state.HomeRecentState.ShowItems
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import app.carbpeek.feature.home.ui.state.HomeStatePrototype as Prototype

internal class HomeStateFactory(private val resources: HomeResources) {

    fun createFor(prototype: Prototype): HomeState {
        val recent = prototype.recent()
        return HomeState(
            profileLabel = resources.profileLabel(prototype.dietPreferences),
            recent = recent,
            showSeeAll = recent is ShowItems,
        )
    }

    private fun Prototype.recent(): HomeRecentState {
        val entries = entries
        val loadError = loadError
        return when {
            loadError != null -> Error(resources.loadError(loadError))
            entries == null -> InitialLoad
            entries.isEmpty() -> Empty
            else -> {
                val today = now.toLocalDateTime(timeZone).date
                val mode = (dietPreferences ?: DietPreferences.Default).dietMode
                ShowItems(entries.map { it.toItemState(today, timeZone, mode) }.toPersistentList())
            }
        }
    }

    private fun ProductAnalysisHistoryEntry.toItemState(
        today: LocalDate,
        timeZone: TimeZone,
        mode: DietMode,
    ): HomeItemState = HomeItemState(
        id = id,
        barcode = productBarcode,
        name = resources.productName(productName),
        icon = category.icon,
        time = timeText(today, timeZone),
        rating = dietRate.toRating(),
        carbs = resources.carbs(carbsPer100g),
        verdictDescription = resources.verdictDescription(dietRate, mode, carbsPer100g),
    )

    private fun ProductAnalysisHistoryEntry.timeText(today: LocalDate, timeZone: TimeZone): UiText =
        when (updateTime.toLocalDateTime(timeZone).date) {
            today -> resources.today(updateTime)
            today.minus(1, DateTimeUnit.DAY) -> resources.yesterday()
            else -> resources.date(updateTime)
        }

    private fun DietRate?.toRating(): Rating = when (this) {
        DietRate.GOOD -> Rating.Positive
        DietRate.NEUTRAL -> Rating.Neutral
        DietRate.NOT_ADVISED -> Rating.Negative
        null -> Rating.Unknown
    }
}
