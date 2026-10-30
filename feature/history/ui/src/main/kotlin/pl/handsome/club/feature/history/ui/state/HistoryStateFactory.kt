package pl.handsome.club.feature.history.ui.state

import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.icon.icon
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.history.ui.HistoryResources
import pl.handsome.club.feature.history.ui.state.HistoryContentState.Empty
import pl.handsome.club.feature.history.ui.state.HistoryContentState.Error
import pl.handsome.club.feature.history.ui.state.HistoryContentState.InitialLoad
import pl.handsome.club.feature.history.ui.state.HistoryContentState.NoResults
import pl.handsome.club.feature.history.ui.state.HistoryContentState.ShowGroups
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.history.ui.state.HistoryStatePrototype as Prototype

internal class HistoryStateFactory(private val resources: HistoryResources) {

    fun createFor(prototype: Prototype): HistoryState {
        val mode = (prototype.dietPreferences ?: DietPreferences.Default).dietMode
        val content = prototype.content(mode)
        return HistoryState(
            query = prototype.query,
            showControls = content is NoResults || content is ShowGroups,
            filters = HistoryFilter.entries.map { filter ->
                HistoryFilterState(
                    filter = filter,
                    label = resources.filterLabel(filter, mode),
                    selected = filter == prototype.filter,
                )
            }.toPersistentList(),
            content = content,
        )
    }

    private fun Prototype.content(mode: DietMode): HistoryContentState {
        val entries = entries
        val loadError = loadError
        return when {
            loadError != null -> Error(resources.loadError(loadError))
            entries == null -> InitialLoad
            entries.isEmpty() -> Empty
            else -> {
                val visible = entries.filter { it.matches(query.trim(), filter) }
                if (visible.isEmpty()) NoResults else ShowGroups(groups(visible, mode))
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
        mode: DietMode,
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

    private fun ProductAnalysisHistoryEntry.toItemState(group: DayGroup, mode: DietMode): HistoryItemState =
        HistoryItemState(
            id = id,
            barcode = productBarcode,
            name = resources.productName(productName),
            icon = category.icon,
            time = if (group == DayGroup.ThisWeek) resources.weekday(updateTime) else resources.time(updateTime),
            rating = dietRate.toRating(),
            carbs = resources.carbs(carbsPer100g),
            verdictDescription = resources.verdictDescription(dietRate, mode, carbsPer100g),
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
