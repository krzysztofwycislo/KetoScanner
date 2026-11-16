package app.carbpeek.feature.favourites.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.favourites.api.model.FavouriteProduct
import app.carbpeek.feature.favourites.ui.state.FavouriteGroupState
import app.carbpeek.feature.favourites.ui.state.FavouriteItemState
import app.carbpeek.feature.favourites.ui.state.FavouritesContentState
import app.carbpeek.feature.favourites.ui.state.FavouritesState
import app.carbpeek.feature.favourites.ui.state.FavouritesStateFactory
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.preferences.api.model.DietPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import app.carbpeek.core.designsystem.R as DesignR
import app.carbpeek.feature.favourites.ui.state.FavouritesStatePrototype as Prototype

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
        assertEquals(
            FavouritesContentState.Empty,
            factory.createFor(Prototype(query = "  ", favourites = emptyList())).content,
        )
    }

    @Test
    fun `when favourites are empty and query is set then content is no results with the trimmed query`() {
        val state = factory.createFor(
            Prototype(query = "  cola ", favourites = emptyList(), favouritesQuery = "  cola "),
        )

        assertEquals(
            FavouritesContentState.NoResults(UiText.Res(R.string.favourites_no_results, UiArg.Text("cola"))),
            state.content,
        )
    }

    @Test
    fun `when query is typed but the empty list was loaded without a query then content is still empty`() {
        val state = factory.createFor(Prototype(query = "cola", favourites = emptyList(), favouritesQuery = ""))

        assertEquals(FavouritesContentState.Empty, state.content)
    }

    @Test
    fun `when query is cleared but the empty list was loaded for a query then content is still no results for it`() {
        val state = factory.createFor(Prototype(query = "", favourites = emptyList(), favouritesQuery = "cola"))

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
    fun `when content is initial load empty or error then search is hidden`() {
        assertFalse(factory.createFor(Prototype.Default).showSearch)
        assertFalse(factory.createFor(Prototype(favourites = emptyList())).showSearch)
        assertFalse(factory.createFor(Prototype(loadError = AppError.Storage)).showSearch)
    }

    @Test
    fun `when content is items or no results then search is shown`() {
        assertTrue(factory.createFor(Prototype(favourites = listOf(favourite("1")))).showSearch)
        assertTrue(
            factory.createFor(Prototype(favourites = emptyList(), favouritesQuery = "cola")).showSearch,
        )
    }

    @Test
    fun `when favourites exist then items map barcodes names icons brands and verdicts in category order`() {
        val state = factory.createFor(
            Prototype(
                favourites = listOf(
                    favourite("2"),
                    favourite("1", dietRate = DietRate.GOOD, carbs = 3.5, category = ProductCategory.CHEESE),
                ),
            ),
        )

        val items = groups(state).flatMap { it.items }
        assertEquals(
            listOf(
                FavouriteItemState(
                    barcode = "1",
                    name = UiText.Raw("Name 1"),
                    icon = AppIcons.Cheese,
                    brand = UiText.Raw("Brand 1"),
                    rating = Rating.Positive,
                    carbs = UiText.Res(R.string.favourites_carbs, UiArg.Numeric.Grams(3.5)),
                    verdictDescription = withCarbs(R.string.favourites_verdict_good_keto, 3.5),
                ),
                FavouriteItemState(
                    barcode = "2",
                    name = UiText.Raw("Name 2"),
                    icon = AppIcons.Bag,
                    brand = UiText.Raw("Brand 2"),
                    rating = Rating.Unknown,
                    carbs = UiText.Res(R.string.favourites_carbs_missing),
                    verdictDescription = UiText.Res(R.string.favourites_verdict_unknown),
                ),
            ),
            items,
        )
    }

    @Test
    fun `when favourites have categories then groups follow the category order with other last`() {
        val state = factory.createFor(
            Prototype(
                favourites = listOf(
                    favourite("1", category = ProductCategory.OTHER),
                    favourite("2", category = ProductCategory.MEAT),
                    favourite("3", category = ProductCategory.SPREADS),
                    favourite("4", category = ProductCategory.MEAT),
                ),
            ),
        )

        val groups = groups(state)
        assertEquals(
            listOf(ProductCategory.SPREADS, ProductCategory.MEAT, ProductCategory.OTHER),
            groups.map { it.category },
        )
        assertEquals(listOf(listOf("3"), listOf("2", "4"), listOf("1")), groups.map { g -> g.items.map { it.barcode } })
    }

    @Test
    fun `when a group is built then it has the category icon label and count and is expanded`() {
        val group = groups(
            factory.createFor(
                Prototype(
                    favourites = listOf(
                        favourite("1", category = ProductCategory.CHEESE),
                        favourite("2", category = ProductCategory.CHEESE),
                    ),
                ),
            ),
        ).single()

        assertEquals(AppIcons.Cheese, group.icon)
        assertEquals(UiText.Res(DesignR.string.designsystem_category_cheese), group.label)
        assertEquals(UiText.Res(R.string.favourites_group_count, UiArg.Numeric.Count(2)), group.count)
        assertTrue(group.expanded)
        assertTrue(group.canToggle)
    }

    @Test
    fun `when a category is collapsed then only its group is not expanded`() {
        val state = factory.createFor(
            Prototype(
                favourites = listOf(
                    favourite("1", category = ProductCategory.CHEESE),
                    favourite("2", category = ProductCategory.MEAT),
                ),
                collapsed = setOf(ProductCategory.MEAT, ProductCategory.FISH),
            ),
        )

        assertEquals(listOf(true, false), groups(state).map { it.expanded })
        assertEquals(listOf(true, true), groups(state).map { it.canToggle })
    }

    @Test
    fun `when a search query is applied then every group is expanded and cannot be toggled`() {
        val state = factory.createFor(
            Prototype(
                query = "a",
                favourites = listOf(
                    favourite("1", category = ProductCategory.CHEESE),
                    favourite("2", category = ProductCategory.MEAT),
                ),
                favouritesQuery = "a",
                collapsed = setOf(ProductCategory.MEAT),
            ),
        )

        assertEquals(listOf(true, true), groups(state).map { it.expanded })
        assertEquals(listOf(false, false), groups(state).map { it.canToggle })
    }

    @Test
    fun `when a group is expanded or collapsed then its toggle state says so`() {
        val state = factory.createFor(
            Prototype(
                favourites = listOf(
                    favourite("1", category = ProductCategory.CHEESE),
                    favourite("2", category = ProductCategory.MEAT),
                ),
                collapsed = setOf(ProductCategory.MEAT),
            ),
        )

        assertEquals(
            listOf(UiText.Res(R.string.favourites_group_expanded), UiText.Res(R.string.favourites_group_collapsed)),
            groups(state).map { it.toggleState },
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

    @Test
    fun `when diet rate is set then it maps to the matching rating`() {
        assertEquals(Rating.Positive, singleItem(favourite("1", dietRate = DietRate.GOOD)).rating)
        assertEquals(Rating.Neutral, singleItem(favourite("1", dietRate = DietRate.NEUTRAL)).rating)
        assertEquals(Rating.Negative, singleItem(favourite("1", dietRate = DietRate.NOT_ADVISED)).rating)
        assertEquals(Rating.Unknown, singleItem(favourite("1", dietRate = null)).rating)
    }

    @Test
    fun `when carbs are present then carbs text has the grams`() {
        assertEquals(
            UiText.Res(R.string.favourites_carbs, UiArg.Numeric.Grams(12.0)),
            singleItem(favourite("1", carbs = 12.0)).carbs,
        )
    }

    @Test
    fun `when carbs are missing then carbs text is no data`() {
        assertEquals(UiText.Res(R.string.favourites_carbs_missing), singleItem(favourite("1", carbs = null)).carbs)
    }

    @Test
    fun `when mode is keto then verdict description uses the keto strings`() {
        val keto = DietPreferences(80, DietMode.KETO)

        assertEquals(
            withCarbs(R.string.favourites_verdict_good_keto, 2.0),
            singleItem(favourite("1", dietRate = DietRate.GOOD, carbs = 2.0), keto).verdictDescription,
        )
        assertEquals(
            withCarbs(R.string.favourites_verdict_not_advised_keto, 40.0),
            singleItem(favourite("1", dietRate = DietRate.NOT_ADVISED, carbs = 40.0), keto).verdictDescription,
        )
    }

    @Test
    fun `when mode is low carb then verdict description uses the low carb strings`() {
        val lowCarb = DietPreferences(80, DietMode.LOW_CARB)

        assertEquals(
            withCarbs(R.string.favourites_verdict_good_low_carb, 2.0),
            singleItem(favourite("1", dietRate = DietRate.GOOD, carbs = 2.0), lowCarb).verdictDescription,
        )
        assertEquals(
            withCarbs(R.string.favourites_verdict_not_advised_low_carb, 40.0),
            singleItem(favourite("1", dietRate = DietRate.NOT_ADVISED, carbs = 40.0), lowCarb).verdictDescription,
        )
        assertEquals(
            withCarbs(R.string.favourites_verdict_neutral, 9.0),
            singleItem(favourite("1", dietRate = DietRate.NEUTRAL, carbs = 9.0), lowCarb).verdictDescription,
        )
    }

    @Test
    fun `when preferences are not loaded then verdict description falls back to keto`() {
        assertEquals(
            withCarbs(R.string.favourites_verdict_good_keto, 2.0),
            singleItem(favourite("1", dietRate = DietRate.GOOD, carbs = 2.0)).verdictDescription,
        )
    }

    @Test
    fun `when carbs are missing then verdict description is the bare verdict`() {
        assertEquals(
            UiText.Res(R.string.favourites_verdict_good_keto),
            singleItem(favourite("1", dietRate = DietRate.GOOD, carbs = null)).verdictDescription,
        )
    }

    private fun singleItem(favourite: FavouriteProduct, preferences: DietPreferences? = null): FavouriteItemState =
        groups(factory.createFor(Prototype(favourites = listOf(favourite), dietPreferences = preferences)))
            .single()
            .items
            .single()

    private fun groups(state: FavouritesState): List<FavouriteGroupState> =
        assertIs<FavouritesContentState.ShowGroups>(state.content).groups

    private fun withCarbs(verdict: Int, carbs: Double): UiText = UiText.Res(
        R.string.favourites_verdict_with_carbs,
        UiArg.Nested(UiText.Res(verdict)),
        UiArg.Nested(UiText.Res(R.string.favourites_carbs_per_100g, UiArg.Numeric.Grams(carbs))),
    )

    private companion object {
        fun favourite(
            barcode: String,
            name: String = "Name $barcode",
            brand: String = "Brand $barcode",
            dietRate: DietRate? = null,
            carbs: Double? = null,
            category: ProductCategory = ProductCategory.OTHER,
        ) = FavouriteProduct(
            barcode,
            name,
            brand,
            Instant.fromEpochMilliseconds(1_700_000_000_000),
            null,
            dietRate,
            carbs,
            category,
        )
    }
}
