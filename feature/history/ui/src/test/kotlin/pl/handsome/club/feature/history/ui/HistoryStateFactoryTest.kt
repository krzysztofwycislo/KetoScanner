package pl.handsome.club.feature.history.ui

import kotlinx.collections.immutable.persistentListOf
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.history.api.model.ProductAnalysisHistoryEntry
import pl.handsome.club.feature.history.ui.state.HistoryContentState
import pl.handsome.club.feature.history.ui.state.HistoryItemState
import pl.handsome.club.feature.history.ui.state.HistoryStateFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import pl.handsome.club.feature.history.ui.state.HistoryStatePrototype as Prototype

class HistoryStateFactoryTest {

    private val factory = HistoryStateFactory(HistoryResources())

    @Test
    fun `when prototype is default then content is initial load`() {
        assertEquals(HistoryContentState.InitialLoad, factory.createFor(Prototype.Default).content)
    }

    @Test
    fun `when load error is set then content is error with load error message`() {
        val state = factory.createFor(Prototype(entries = null, loadError = AppError.Storage))

        assertEquals(
            HistoryContentState.Error(UiText.Res(pl.handsome.club.core.designsystem.R.string.designsystem_error_storage)),
            state.content,
        )
    }

    @Test
    fun `when load error is network then content is error with network message`() {
        val state = factory.createFor(Prototype(entries = null, loadError = AppError.Network))

        assertEquals(
            HistoryContentState.Error(UiText.Res(pl.handsome.club.core.designsystem.R.string.designsystem_error_network)),
            state.content,
        )
    }

    @Test
    fun `when load error is set and entries exist then content is still error`() {
        val state = factory.createFor(Prototype(entries = listOf(entry(1)), loadError = AppError.Storage))

        assertIs<HistoryContentState.Error>(state.content)
    }

    @Test
    fun `when entries are empty then content is empty`() {
        assertEquals(HistoryContentState.Empty, factory.createFor(Prototype(entries = emptyList())).content)
    }

    @Test
    fun `when entries exist then items keep order ids and barcodes`() {
        val state = factory.createFor(Prototype(entries = listOf(entry(3), entry(1), entry(2))))

        val items = assertIs<HistoryContentState.ShowItems>(state.content).items
        assertEquals(listOf(3L, 1L, 2L), items.map { it.id })
        assertEquals(listOf("barcode-3", "barcode-1", "barcode-2"), items.map { it.barcode })
    }

    @Test
    fun `when entry has name and brand then they are raw texts`() {
        val item = singleItem(entry(1, name = "Peanut butter", brand = "Nuts & Co"))

        assertEquals(UiText.Raw("Peanut butter"), item.name)
        assertEquals(UiText.Raw("Nuts & Co"), item.brand)
    }

    @Test
    fun `when entry name is blank then name is unnamed product`() {
        val item = singleItem(entry(1, name = "  "))

        assertEquals(UiText.Res(R.string.history_unnamed_product), item.name)
    }

    @Test
    fun `when diet rate is set or missing then rating and description match`() {
        val expected = mapOf(
            DietRate.GOOD to (Rating.Positive to R.string.history_rating_good),
            DietRate.NEUTRAL to (Rating.Neutral to R.string.history_rating_neutral),
            DietRate.NOT_ADVISED to (Rating.Negative to R.string.history_rating_not_advised),
            null to (Rating.Unknown to R.string.history_rating_unknown),
        )

        expected.forEach { (dietRate, ratingAndText) ->
            val item = singleItem(entry(1, dietRate = dietRate))

            assertEquals(ratingAndText.first, item.rating)
            assertEquals(UiText.Res(ratingAndText.second), item.ratingDescription)
        }
    }

    @Test
    fun `when items are shown then subtitle is last searches plural for the entry count`() {
        val state = factory.createFor(Prototype(entries = listOf(entry(1), entry(2), entry(3))))

        assertEquals(
            UiText.Plural(R.plurals.history_last_searches, 3, persistentListOf(UiArg.Numeric.Count(3))),
            state.subtitle,
        )
    }

    @Test
    fun `when a single item is shown then subtitle uses the count of one`() {
        val state = factory.createFor(Prototype(entries = listOf(entry(1))))

        assertEquals(
            UiText.Plural(R.plurals.history_last_searches, 1, persistentListOf(UiArg.Numeric.Count(1))),
            state.subtitle,
        )
    }

    @Test
    fun `when loading empty or failed then there is no subtitle`() {
        assertNull(factory.createFor(Prototype.Default).subtitle)
        assertNull(factory.createFor(Prototype(entries = emptyList())).subtitle)
        assertNull(factory.createFor(Prototype(entries = null, loadError = AppError.Storage)).subtitle)
        assertNull(factory.createFor(Prototype(entries = listOf(entry(1)), loadError = AppError.Storage)).subtitle)
    }

    @Test
    fun `when menu open flag is set or not then it is passed through`() {
        assertTrue(factory.createFor(Prototype(isMenuOpen = true)).isMenuOpen)
        assertFalse(factory.createFor(Prototype(isMenuOpen = false)).isMenuOpen)
    }

    private fun singleItem(entry: ProductAnalysisHistoryEntry): HistoryItemState =
        assertIs<HistoryContentState.ShowItems>(factory.createFor(Prototype(entries = listOf(entry))).content)
            .items
            .single()

    private companion object {
        fun entry(
            id: Long,
            name: String = "Name $id",
            brand: String = "Brand $id",
            dietRate: DietRate? = DietRate.GOOD,
        ) = ProductAnalysisHistoryEntry(
            id = id,
            productName = name,
            productBarcode = "barcode-$id",
            productBrand = brand,
            updateTime = Instant.fromEpochMilliseconds(1_700_000_000_000),
            dietRate = dietRate,
            carbsPer100g = null,
        )
    }
}
