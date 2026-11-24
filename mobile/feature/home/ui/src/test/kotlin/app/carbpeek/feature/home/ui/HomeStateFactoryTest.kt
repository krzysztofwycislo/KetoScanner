package app.carbpeek.feature.home.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.designsystem.text.toUiText
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.history.api.model.ProductAnalysisHistoryEntry
import app.carbpeek.feature.home.ui.state.HomeItemState
import app.carbpeek.feature.home.ui.state.HomeRecentState
import app.carbpeek.feature.home.ui.state.HomeStateFactory
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import app.carbpeek.feature.home.ui.state.HomeStatePrototype as Prototype

class HomeStateFactoryTest {

    private val factory = HomeStateFactory(HomeResources())

    @Test
    fun `when prototype is default then recent is initial load`() {
        assertEquals(HomeRecentState.InitialLoad, factory.createFor(Prototype.Default).recent)
    }

    @Test
    fun `when load error is set then recent is error with the error message`() {
        val state = factory.createFor(Prototype(loadError = AppError.Storage))

        assertEquals(HomeRecentState.Error(AppError.Storage.toUiText()), state.recent)
    }

    @Test
    fun `when load error is set with entries then recent is still error`() {
        val state = factory.createFor(Prototype(entries = listOf(entry(1)), loadError = AppError.Storage))

        assertIs<HomeRecentState.Error>(state.recent)
    }

    @Test
    fun `when entries are empty then recent is empty`() {
        assertEquals(HomeRecentState.Empty, factory.createFor(Prototype(entries = emptyList())).recent)
    }

    @Test
    fun `when entries are present then items keep order ids and barcodes`() {
        val entries = listOf(entry(3), entry(1), entry(2))

        val recent = assertIs<HomeRecentState.ShowItems>(factory.createFor(prototype(entries)).recent)

        assertEquals(listOf(3L, 1L, 2L), recent.items.map { it.id })
        assertEquals(listOf("barcode-3", "barcode-1", "barcode-2"), recent.items.map { it.barcode })
    }

    @Test
    fun `when recent has items then see all is shown`() {
        assertTrue(factory.createFor(prototype(listOf(entry(1)))).showSeeAll)
    }

    @Test
    fun `when recent is initial load then see all is hidden`() {
        assertFalse(factory.createFor(Prototype.Default).showSeeAll)
    }

    @Test
    fun `when recent is empty then see all is hidden`() {
        assertFalse(factory.createFor(Prototype(entries = emptyList())).showSeeAll)
    }

    @Test
    fun `when recent is error then see all is hidden even with entries`() {
        assertFalse(factory.createFor(Prototype(entries = listOf(entry(1)), loadError = AppError.Storage)).showSeeAll)
    }

    @Test
    fun `when entry has a category then item icon follows it`() {
        assertEquals(AppIcons.Cheese, item(entry(1, category = ProductCategory.CHEESE)).icon)
        assertEquals(AppIcons.Bag, item(entry(2)).icon)
    }

    @Test
    fun `when entry is from the same day then time is today with the time`() {
        val time = Instant.parse("2026-10-12T08:15:00Z")

        assertEquals(UiText.Res(R.string.home_time_today, UiArg.Time(time)), item(entry(1, time = time)).time)
    }

    @Test
    fun `when entry is from the previous day then time is yesterday`() {
        val time = Instant.parse("2026-10-11T23:59:00Z")

        assertEquals(UiText.Res(R.string.home_time_yesterday), item(entry(1, time = time)).time)
    }

    @Test
    fun `when entry is older than yesterday then time is the date`() {
        val time = Instant.parse("2026-10-10T12:00:00Z")

        assertEquals(UiText.Res(R.string.home_time_date, UiArg.Date(time)), item(entry(1, time = time)).time)
    }

    @Test
    fun `when scan is just after local midnight in a non UTC zone then the local day is used`() {
        val warsaw = TimeZone.of("Europe/Warsaw")
        val now = Instant.parse("2026-10-13T08:00:00Z")
        val afterMidnight = Instant.parse("2026-10-12T22:30:00Z")
        val beforeMidnight = Instant.parse("2026-10-12T21:30:00Z")
        val prototype = Prototype(
            entries = listOf(entry(1, time = afterMidnight), entry(2, time = beforeMidnight)),
            now = now,
            timeZone = warsaw,
        )

        val items = assertIs<HomeRecentState.ShowItems>(factory.createFor(prototype).recent).items

        assertEquals(UiText.Res(R.string.home_time_today, UiArg.Time(afterMidnight)), items[0].time)
        assertEquals(UiText.Res(R.string.home_time_yesterday), items[1].time)
    }

    @Test
    fun `when carbs are present then pill shows grams`() {
        assertEquals(UiText.Res(R.string.home_carbs, UiArg.Numeric.Grams(14.0)), item(entry(1, carbs = 14.0)).carbs)
    }

    @Test
    fun `when carbs are missing then pill shows no data`() {
        assertEquals(UiText.Res(R.string.home_carbs_missing), item(entry(1, carbs = null)).carbs)
    }

    @Test
    fun `when diet rate is mapped then rating matches`() {
        assertEquals(Rating.Positive, item(entry(1, rate = DietRate.GOOD)).rating)
        assertEquals(Rating.Neutral, item(entry(1, rate = DietRate.NEUTRAL)).rating)
        assertEquals(Rating.Negative, item(entry(1, rate = DietRate.NOT_ADVISED)).rating)
        assertEquals(Rating.Unknown, item(entry(1, rate = null)).rating)
    }

    @Test
    fun `when mode is keto then verdict descriptions use keto wording`() {
        val keto = DietPreferences(40, DietMode.KETO)

        assertEquals(
            UiText.Res(R.string.home_verdict_good_keto),
            item(entry(1, rate = DietRate.GOOD), keto).verdictDescription,
        )
        assertEquals(
            UiText.Res(R.string.home_verdict_not_advised_keto),
            item(entry(1, rate = DietRate.NOT_ADVISED), keto).verdictDescription,
        )
        assertEquals(
            UiText.Res(R.string.home_verdict_neutral),
            item(entry(1, rate = DietRate.NEUTRAL), keto).verdictDescription,
        )
        assertEquals(UiText.Res(R.string.home_verdict_unknown), item(entry(1, rate = null), keto).verdictDescription)
    }

    @Test
    fun `when mode is low carb then verdict descriptions use low carb wording`() {
        val lowCarb = DietPreferences(100, DietMode.LOW_CARB)

        assertEquals(
            UiText.Res(R.string.home_verdict_good_low_carb),
            item(entry(1, rate = DietRate.GOOD), lowCarb).verdictDescription,
        )
        assertEquals(
            UiText.Res(R.string.home_verdict_not_advised_low_carb),
            item(entry(1, rate = DietRate.NOT_ADVISED), lowCarb).verdictDescription,
        )
    }

    @Test
    fun `when carbs are present then verdict description includes carbs per 100 g`() {
        val description = item(entry(1, rate = DietRate.GOOD, carbs = 3.5), DietPreferences(40, DietMode.LOW_CARB))
            .verdictDescription

        assertEquals(
            UiText.Res(
                R.string.home_verdict_with_carbs,
                UiArg.Nested(UiText.Res(R.string.home_verdict_good_low_carb)),
                UiArg.Nested(UiText.Res(R.string.home_carbs_per_100g, UiArg.Numeric.Grams(3.5))),
            ),
            description,
        )
    }

    @Test
    fun `when preferences are not loaded then verdict falls back to keto wording`() {
        assertEquals(
            UiText.Res(R.string.home_verdict_good_keto),
            item(entry(1, rate = DietRate.GOOD), null).verdictDescription,
        )
    }

    @Test
    fun `when preferences are null then profile label is the default`() {
        assertEquals(UiText.Res(R.string.home_profile_default), factory.createFor(Prototype.Default).profileLabel)
    }

    @Test
    fun `when preferences are keto 20 then profile label is the strict keto preset`() {
        val state = factory.createFor(Prototype(dietPreferences = DietPreferences(20, DietMode.KETO)))

        assertEquals(UiText.Res(R.string.home_preset_strict_keto), state.profileLabel)
    }

    @Test
    fun `when preferences are keto 40 then profile label is the keto preset`() {
        val state = factory.createFor(Prototype(dietPreferences = DietPreferences(40, DietMode.KETO)))

        assertEquals(UiText.Res(R.string.home_preset_keto), state.profileLabel)
    }

    @Test
    fun `when preferences are low carb 100 then profile label is the low carb preset`() {
        val state = factory.createFor(Prototype(dietPreferences = DietPreferences(100, DietMode.LOW_CARB)))

        assertEquals(UiText.Res(R.string.home_preset_low_carb), state.profileLabel)
    }

    @Test
    fun `when preferences match no preset then profile label is the keto preset`() {
        val state = factory.createFor(Prototype(dietPreferences = DietPreferences(35, DietMode.KETO)))

        assertEquals(UiText.Res(R.string.home_preset_keto), state.profileLabel)
    }

    @Test
    fun `when name is blank then unnamed product is shown`() {
        assertEquals(UiText.Res(R.string.home_unnamed_product), item(entry(1, name = " ")).name)
        assertEquals(UiText.Raw("Name 1"), item(entry(1)).name)
    }

    private fun prototype(entries: List<ProductAnalysisHistoryEntry>, preferences: DietPreferences? = null) =
        Prototype(entries = entries, dietPreferences = preferences, now = NOW, timeZone = TimeZone.UTC)

    private fun item(entry: ProductAnalysisHistoryEntry, preferences: DietPreferences? = null): HomeItemState =
        assertIs<HomeRecentState.ShowItems>(factory.createFor(prototype(listOf(entry), preferences)).recent)
            .items
            .single()

    private companion object {
        val NOW = Instant.parse("2026-10-12T18:42:00Z")

        fun entry(
            id: Long,
            name: String = "Name $id",
            time: Instant = NOW,
            rate: DietRate? = null,
            carbs: Double? = null,
            category: ProductCategory = ProductCategory.OTHER,
        ) = ProductAnalysisHistoryEntry(
            id = id,
            productName = name,
            productBarcode = "barcode-$id",
            productBrand = "Brand",
            updateTime = time,
            dietRate = rate,
            carbsPer100g = carbs,
            category = category,
        )
    }
}
