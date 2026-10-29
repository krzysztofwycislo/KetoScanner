package pl.handsome.club.feature.favourites.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.favourites.ui.state.FavouriteItemState
import pl.handsome.club.feature.favourites.ui.state.FavouritesContentState
import pl.handsome.club.feature.favourites.ui.state.FavouritesStateFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant
import pl.handsome.club.core.designsystem.R as DesignR
import pl.handsome.club.feature.favourites.ui.state.FavouritesStatePrototype as Prototype

class FavouritesStateFactoryTest {

    private val factory = FavouritesStateFactory(FavouritesResources())

    @Test
    fun `when prototype is default then content is initial load and query is empty`() {
        val state = factory.createFor(Prototype.Default)

        assertEquals(FavouritesContentState.InitialLoad, state.content)
        assertEquals("", state.query)
    }

    @Test
    fun `when load error is set then content is error even if favourites exist`() {
        val state = factory.createFor(Prototype(favourites = listOf(favourite("1")), loadError = AppError.Storage))

        assertEquals(FavouritesContentState.Error(UiText.Res(DesignR.string.designsystem_error_storage)), state.content)
    }

    @Test
    fun `when favourites are empty and query is blank then content is empty`() {
        assertEquals(FavouritesContentState.Empty, factory.createFor(Prototype(query = "  ", favourites = emptyList())).content)
    }

    @Test
    fun `when favourites are empty and query is set then content is no results with the trimmed query`() {
        val state = factory.createFor(Prototype(query = "  cola ", favourites = emptyList()))

        assertEquals(
            FavouritesContentState.NoResults(UiText.Res(R.string.favourites_no_results, UiArg.Text("cola"))),
            state.content,
        )
    }

    @Test
    fun `when query is set then it is passed through untrimmed`() {
        assertEquals(" cola", factory.createFor(Prototype(query = " cola")).query)
    }

    @Test
    fun `when favourites exist then items keep order barcodes names brands and images`() {
        val state = factory.createFor(Prototype(favourites = listOf(favourite("2"), favourite("1", imageUrl = IMAGE_URL))))

        val items = assertIs<FavouritesContentState.ShowItems>(state.content).items
        assertEquals(
            listOf(
                FavouriteItemState("2", UiText.Raw("Name 2"), UiText.Raw("Brand 2"), null),
                FavouriteItemState("1", UiText.Raw("Name 1"), UiText.Raw("Brand 1"), IMAGE_URL),
            ),
            items,
        )
    }

    @Test
    fun `when favourite name is blank then name is unnamed product`() {
        val item = singleItem(favourite("1", name = " "))

        assertEquals(UiText.Res(R.string.favourites_unnamed_product), item.name)
    }

    @Test
    fun `when favourite brand is blank then brand is null`() {
        val item = singleItem(favourite("1", brand = ""))

        assertEquals(null, item.brand)
    }

    private fun singleItem(favourite: FavouriteProduct): FavouriteItemState =
        assertIs<FavouritesContentState.ShowItems>(factory.createFor(Prototype(favourites = listOf(favourite))).content)
            .items
            .single()

    private companion object {
        const val IMAGE_URL = "https://example.com/image.jpg"

        fun favourite(
            barcode: String,
            name: String = "Name $barcode",
            brand: String = "Brand $barcode",
            imageUrl: String? = null,
        ) = FavouriteProduct(barcode, name, brand, Instant.fromEpochMilliseconds(1_700_000_000_000), imageUrl)
    }
}
