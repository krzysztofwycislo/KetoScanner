package app.carbpeek.feature.history.ui.state

import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.icon
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.history.ui.HistoryResources
import app.carbpeek.feature.history.ui.state.HistoryContentState.Empty
import app.carbpeek.feature.history.ui.state.HistoryContentState.Error
import app.carbpeek.feature.history.ui.state.HistoryContentState.InitialLoad
import app.carbpeek.feature.history.ui.state.HistoryContentState.NoResults
import app.carbpeek.feature.history.ui.state.HistoryContentState.ShowGroups
import app.carbpeek.feature.preferences.api.model.DietMode
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import app.carbpeek.feature.history.ui.state.HistoryStatePrototype as Prototype

internal class HistoryStateFactory(private val resources: HistoryResources) {

    fun createFor(prototype: Prototype): HistoryState {
        val mode = prototype.dietPreferences?.dietMode
        val content = prototype.content(mode)
        return HistoryState(
            query = prototype.query,
            showControls = content is NoResults || content is ShowGroups,
            filters = prototype.filters(mode),
            content = content,
        )
    }

    private fun Prototype.filters(mode: DietMode?): PersistentList<HistoryFilterState> {
        if (mode == null) {
            return persistentListOf()
        }
        return HistoryFilter.entries.map { option ->
            HistoryFilterState(
                filter = option,
                label = resources.filterLabel(filter = option, mode = mode),
                selected = option == filter,
            )
        }.toPersistentList()
    }

    private fun Prototype.content(mode: DietMode?): HistoryContentState {
        val entries = entries
        val loadError = loadError
        return when {
            loadError != null -> Error(resources.loadError(loadError))
            entries == null -> InitialLoad
            entries.isEmpty() -> Empty
            dietPreferences == null -> InitialLoad
            else -> {
                val offeredFilter = if (mode == null) HistoryFilter.ALL else filter
                val visible = entries.filter { it.matches(query = query.trim(), filter = offeredFilter) }
                if (visible.isEmpty()) {
                    NoResults(resources.noResults(mode))
                } else {
                    ShowGroups(groups(entries = visible, mode = mode))
                }
            }
        }
    }

    private fun ProductAnalysisHistoryEntry.matches(query: String, filter: HistoryFilter): Boolean {
        val matchesQuery = query.isEmpty() || productName.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            HistoryFilter.ALL -> true
            HistoryFilter.GOOD -> dietRate == DietRate.GOOD
            HistoryFilter.NEUTRAL -> dietRate == DietRate.NEUTRAL
            HistoryFilter.NOT_ADVISED -> dietRate == DietRate.NOT_ADVISED
        }
        return matchesQuery && matchesFilter
    }

    private fun Prototype.groups(
        entries: List<ProductAnalysisHistoryEntry>,
        mode: DietMode?,
    ) = entries.groupBy { groupOf(it.updateTime.toLocalDateTime(timeZone).date) }
        .map { (group, groupEntries) ->
            HistoryGroupState(
                key = group.key,
                title = group.title(groupEntries.first()),
                items = groupEntries.map { it.toItemState(group, mode) }.toPersistentList(),
            )
        }
        .toPersistentList()

    private fun Prototype.groupOf(date: LocalDate): DayGroup {
        val today = now.toLocalDateTime(timeZone).date
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        val weekStart = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
        return when {
            date >= today -> DayGroup.Today
            date == yesterday -> DayGroup.Yesterday
            date >= weekStart && date < yesterday -> DayGroup.ThisWeek
            else -> DayGroup.OnDate(date)
        }
    }

    private fun DayGroup.title(first: ProductAnalysisHistoryEntry): UiText = when (this) {
        DayGroup.Today -> resources.today()
        DayGroup.Yesterday -> resources.yesterday()
        DayGroup.ThisWeek -> resources.thisWeek()
        is DayGroup.OnDate -> resources.dateGroup(first.updateTime)
    }

    private fun ProductAnalysisHistoryEntry.toItemState(group: DayGroup, mode: DietMode?): HistoryItemState =
        HistoryItemState(
            id = id,
            barcode = productBarcode,
            name = resources.productName(productName),
            icon = category.icon,
            time = if (group == DayGroup.ThisWeek) resources.weekday(updateTime) else resources.time(updateTime),
            rating = if (mode == null) null else dietRate.toRating(),
            carbs = resources.carbs(grams = carbsPer100g, mode = mode),
            verdictDescription = resources.verdictDescription(rate = dietRate, mode = mode, carbs = carbsPer100g),
        )

    private fun DietRate?.toRating(): Rating = when (this) {
        DietRate.GOOD -> Rating.Positive
        DietRate.NEUTRAL -> Rating.Neutral
        DietRate.NOT_ADVISED -> Rating.Negative
        null -> Rating.Unknown
    }

    private sealed interface DayGroup {
        val key: String

        data object Today : DayGroup {
            override val key = "today"
        }

        data object Yesterday : DayGroup {
            override val key = "yesterday"
        }

        data object ThisWeek : DayGroup {
            override val key = "week"
        }

        data class OnDate(val date: LocalDate) : DayGroup {
            override val key = "date-$date"
        }
    }
}
