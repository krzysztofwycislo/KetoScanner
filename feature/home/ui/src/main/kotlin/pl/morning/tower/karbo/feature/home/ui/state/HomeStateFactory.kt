package pl.morning.tower.karbo.feature.home.ui.state

import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import pl.morning.tower.karbo.core.designsystem.component.Rating
import pl.morning.tower.karbo.core.designsystem.icon.icon
import pl.morning.tower.karbo.core.designsystem.text.UiText
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.morning.tower.karbo.feature.home.ui.HomeResources
import pl.morning.tower.karbo.feature.home.ui.state.HomeRecentState.Empty
import pl.morning.tower.karbo.feature.home.ui.state.HomeRecentState.Error
import pl.morning.tower.karbo.feature.home.ui.state.HomeRecentState.InitialLoad
import pl.morning.tower.karbo.feature.home.ui.state.HomeRecentState.ShowItems
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.home.ui.state.HomeStatePrototype as Prototype

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
