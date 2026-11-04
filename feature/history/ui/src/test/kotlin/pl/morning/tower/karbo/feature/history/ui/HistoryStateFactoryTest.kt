package pl.handsome.club.feature.history.ui

import kotlinx.datetime.TimeZone
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.designsystem.text.toUiText
import pl.handsome.club.core.model.ProductCategory
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.history.ui.state.HistoryContentState
import pl.handsome.club.feature.history.ui.state.HistoryFilter
import pl.handsome.club.feature.history.ui.state.HistoryState
import pl.handsome.club.feature.history.ui.state.HistoryStateFactory
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import pl.handsome.club.feature.history.ui.state.HistoryStatePrototype as Prototype

class HistoryStateFactoryTest {

    private val factory = HistoryStateFactory(HistoryResources())

    @Test
    fun `when prototype is default then content is initial load without controls`() {
        val state = factory.createFor(Prototype.Default)

        assertEquals(HistoryContentState.InitialLoad, state.content)
        assertFalse(state.showControls)
    }

    @Test
    fun `when entries are empty then content is empty without controls`() {
        val state = factory.createFor(prototype(emptyList()))

        assertEquals(HistoryContentState.Empty, state.content)
        assertFalse(state.showControls)
    }

    @Test
    fun `when load error is set then content is error without controls`() {
        val state = factory.createFor(prototype(listOf(entry(1))).copy(loadError = AppError.Storage))

        assertEquals(HistoryContentState.Error(AppError.Storage.toUiText()), state.content)
        assertFalse(state.showControls)
    }

    @Test
    fun `when entries are present then groups are shown with controls and the query is passed through`() {
        val state = factory.createFor(prototype(listOf(entry(1))).copy(query = "na"))

        assertIs<HistoryContentState.ShowGroups>(state.content)
        assertTrue(state.showControls)
        assertEquals("na", state.query)
    }

    @Test
    fun `when entries span this week then today yesterday and this week groups come in order`() {
        val today = Instant.parse("2026-10-15T08:15:00Z")
        val yesterday = Instant.parse("2026-10-14T23:59:00Z")
        val monday = Instant.parse("2026-10-12T10:00:00Z")

        val groups = groups(listOf(entry(1, today), entry(2, yesterday), entry(3, monday)))

        assertEquals(listOf("today", "yesterday", "week"), groups.map { it.key })
        assertEquals(
            listOf(resources.today(), resources.yesterday(), resources.thisWeek()),
            groups.map { it.title },
        )
        assertEquals(UiText.Res(R.string.history_row_time, UiArg.Time(today)), groups[0].items.single().time)
        assertEquals(UiText.Res(R.string.history_row_time, UiArg.Time(yesterday)), groups[1].items.single().time)
        assertEquals(UiText.Res(R.string.history_row_time, UiArg.Weekday(monday)), groups[2].items.single().time)
    }

    @Test
    fun `when entries are older than this week then each date gets its own group`() {
        val sunday = Instant.parse("2026-10-11T20:00:00Z")
        val sundayMorning = Instant.parse("2026-10-11T07:00:00Z")
        val older = Instant.parse("2026-09-30T12:00:00Z")

        val groups = groups(listOf(entry(1, sunday), entry(2, sundayMorning), entry(3, older)))

        assertEquals(listOf("date-2026-10-11", "date-2026-09-30"), groups.map { it.key })
        assertEquals(listOf(resources.dateGroup(sunday), resources.dateGroup(older)), groups.map { it.title })
        assertEquals(listOf(1L, 2L), groups[0].items.map { it.id })
        assertEquals(resources.time(sunday), groups[0].items[0].time)
    }

    @Test
    fun `when now is a Monday then a Saturday entry goes to a date group`() {
        val saturday = Instant.parse("2026-10-10T12:00:00Z")

        val groups = groups(listOf(entry(1, saturday)), now = Instant.parse("2026-10-12T18:42:00Z"))

        assertEquals(listOf("date-2026-10-10"), groups.map { it.key })
    }

    @Test
    fun `when now is a Tuesday then a Sunday entry goes to a date group, not this week`() {
        val sunday = Instant.parse("2026-10-11T12:00:00Z")

        val groups = groups(listOf(entry(1, sunday)), now = Instant.parse("2026-10-13T18:42:00Z"))

        assertEquals(listOf("date-2026-10-11"), groups.map { it.key })
    }

    @Test
    fun `when the zone is Warsaw then an entry just after local midnight is grouped by the local date`() {
        val justAfterMidnight = Instant.parse("2026-10-14T22:30:00Z")
        val warsaw = TimeZone.of("Europe/Warsaw")

        assertEquals(listOf("today"), groups(listOf(entry(1, justAfterMidnight)), timeZone = warsaw).map { it.key })
        assertEquals(listOf("yesterday"), groups(listOf(entry(1, justAfterMidnight))).map { it.key })
    }

    @Test
    fun `when a query is set then entries are matched by name case-insensitively and trimmed`() {
        val entries = listOf(
            entry(1, name = "Peanut Butter"),
            entry(2, name = "Cola"),
            entry(3, name = "Oat cookies", brand = "Butter Bakery"),
        )

        val state = factory.createFor(prototype(entries).copy(query = "  bUTTer "))

        assertEquals(listOf(1L), ids(state))
    }

    @Test
    fun `when a filter is chosen then only its rate is kept and unrated entries appear only under all`() {
        val entries = listOf(
            entry(1, rate = DietRate.GOOD),
            entry(2, rate = DietRate.NEUTRAL),
            entry(3, rate = DietRate.NOT_ADVISED),
            entry(4, rate = null),
        )

        fun idsFor(filter: HistoryFilter) = ids(factory.createFor(prototype(entries).copy(filter = filter)))

        assertEquals(listOf(1L, 2L, 3L, 4L), idsFor(HistoryFilter.ALL))
        assertEquals(listOf(1L), idsFor(HistoryFilter.GOOD))
        assertEquals(listOf(2L), idsFor(HistoryFilter.NEUTRAL))
        assertEquals(listOf(3L), idsFor(HistoryFilter.NOT_ADVISED))
    }

    @Test
    fun `when a query hides everything then content is no results with controls`() {
        val state = factory.createFor(prototype(listOf(entry(1))).copy(query = "xyz"))

        assertEquals(HistoryContentState.NoResults, state.content)
        assertTrue(state.showControls)
    }

    @Test
    fun `when a filter hides everything then content is no results with controls`() {
        val state = factory.createFor(prototype(listOf(entry(1, rate = null))).copy(filter = HistoryFilter.GOOD))

        assertEquals(HistoryContentState.NoResults, state.content)
        assertTrue(state.showControls)
    }

    @Test
    fun `when mode is keto then chip labels are keto labels`() {
        val state = factory.createFor(prototype(listOf(entry(1)), DietPreferences(20, DietMode.KETO)))

        assertEquals(
            listOf(
                R.string.history_filter_all,
                R.string.history_filter_good_keto,
                R.string.history_filter_neutral,
                R.string.history_filter_not_advised_keto,
            ).map { UiText.Res(it) },
            state.filters.map { it.label },
        )
    }

    @Test
    fun `when mode is low carb then chip labels are low carb labels`() {
        val state = factory.createFor(prototype(listOf(entry(1)), DietPreferences(80, DietMode.LOW_CARB)))

        assertEquals(
            listOf(
                R.string.history_filter_all,
                R.string.history_filter_good_low_carb,
                R.string.history_filter_neutral,
                R.string.history_filter_not_advised_low_carb,
            ).map { UiText.Res(it) },
            state.filters.map { it.label },
        )
    }

    @Test
    fun `when preferences are missing then chip labels fall back to keto`() {
        val state = factory.createFor(Prototype.Default)

        assertEquals(UiText.Res(R.string.history_filter_good_keto), state.filters[1].label)
    }

    @Test
    fun `when a filter is chosen then only its chip is selected`() {
        val state = factory.createFor(prototype(listOf(entry(1))).copy(filter = HistoryFilter.NEUTRAL))

        assertEquals(HistoryFilter.entries, state.filters.map { it.filter })
        assertEquals(listOf(false, false, true, false), state.filters.map { it.selected })
    }

    @Test
    fun `when carbs are missing then the row shows no data and the verdict alone`() {
        val item = groups(listOf(entry(1, rate = DietRate.GOOD, carbs = null))).single().items.single()

        assertEquals(UiText.Res(R.string.history_carbs_missing), item.carbs)
        assertEquals(UiText.Res(R.string.history_verdict_good_keto), item.verdictDescription)
        assertEquals(Rating.Positive, item.rating)
    }

    @Test
    fun `when carbs are present then the row shows grams and the verdict with carbs`() {
        val item = groups(listOf(entry(1, rate = DietRate.NOT_ADVISED, carbs = 10.6))).single().items.single()

        assertEquals(UiText.Res(R.string.history_carbs, UiArg.Numeric.Grams(10.6)), item.carbs)
        assertEquals(
            UiText.Res(
                R.string.history_verdict_with_carbs,
                UiArg.Nested(UiText.Res(R.string.history_verdict_not_advised_keto)),
                UiArg.Nested(UiText.Res(R.string.history_carbs_per_100g, UiArg.Numeric.Grams(10.6))),
            ),
            item.verdictDescription,
        )
        assertEquals(Rating.Negative, item.rating)
    }

    @Test
    fun `when the product has no name then the row shows the unnamed text`() {
        val item = groups(listOf(entry(1, name = " "))).single().items.single()

        assertEquals(UiText.Res(R.string.history_unnamed_product), item.name)
        assertEquals("barcode-1", item.barcode)
    }

    @Test
    fun `when the entry has a category then the row icon follows it`() {
        val items = groups(listOf(entry(1, category = ProductCategory.FISH), entry(2))).single().items

        assertEquals(listOf(AppIcons.Fish, AppIcons.Bag), items.map { it.icon })
    }

    private val resources = HistoryResources()

    private fun prototype(
        entries: List<ProductAnalysisHistoryEntry>,
        preferences: DietPreferences? = null,
        now: Instant = NOW,
        timeZone: TimeZone = TimeZone.UTC,
    ) = Prototype(entries = entries, dietPreferences = preferences, now = now, timeZone = timeZone)

    private fun groups(
        entries: List<ProductAnalysisHistoryEntry>,
        now: Instant = NOW,
        timeZone: TimeZone = TimeZone.UTC,
    ) = assertIs<HistoryContentState.ShowGroups>(
        factory.createFor(prototype(entries, now = now, timeZone = timeZone)).content
    ).groups

    private fun ids(state: HistoryState) =
        assertIs<HistoryContentState.ShowGroups>(state.content).groups.flatMap { group -> group.items.map { it.id } }

    private companion object {
        val NOW = Instant.parse("2026-10-15T18:42:00Z")

        fun entry(
            id: Long,
            time: Instant = NOW,
            name: String = "Name $id",
            brand: String = "Brand",
            rate: DietRate? = null,
            carbs: Double? = null,
            category: ProductCategory = ProductCategory.OTHER,
        ) = ProductAnalysisHistoryEntry(
            id = id,
            productName = name,
            productBarcode = "barcode-$id",
            productBrand = brand,
            updateTime = time,
            dietRate = rate,
            carbsPer100g = carbs,
            category = category,
        )
    }
}
