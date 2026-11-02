package pl.handsome.club.feature.product.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.icon.AppIcons
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.core.model.ProductCategory
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.analysis.api.model.RatingReason
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.feature.product.ui.state.CategoryChipState
import pl.handsome.club.feature.product.ui.state.FavouriteButtonState
import pl.handsome.club.feature.product.ui.state.ImagePreviewState
import pl.handsome.club.feature.product.ui.state.ProductContentState
import pl.handsome.club.feature.product.ui.state.ProductStateFactory
import pl.handsome.club.feature.product.ui.state.VerdictState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pl.handsome.club.core.designsystem.R as DesignR
import pl.handsome.club.feature.product.ui.state.ProductStatePrototype as Prototype

class ProductStateFactoryTest {

    private val factory = ProductStateFactory(ProductResources())

    @Test
    fun `when prototype is default then content is loading`() {
        assertEquals(ProductContentState.Loading, factory.createFor(Prototype.Default).content)
    }

    @Test
    fun `when load error is not found then content is error with not found message`() {
        val state = factory.createFor(Prototype(loadError = AppError.NotFound))

        assertEquals(
            ProductContentState.Error(UiText.Res(DesignR.string.designsystem_error_not_found)),
            state.content,
        )
    }

    @Test
    fun `when load error is network then content is error with network message`() {
        val state = factory.createFor(Prototype(loadError = AppError.Network))

        assertEquals(
            ProductContentState.Error(UiText.Res(DesignR.string.designsystem_error_network)),
            state.content,
        )
    }

    @Test
    fun `when load error is set and analysis exists then content is still error`() {
        val state = factory.createFor(Prototype(analysis = rated(DietRate.GOOD), loadError = AppError.Network))

        assertIs<ProductContentState.Error>(state.content)
    }

    @Test
    fun `when analysis is rated then verdict has rating summary with mode and portion`() {
        val ratings = mapOf(
            DietRate.GOOD to Rating.Positive,
            DietRate.NEUTRAL to Rating.Neutral,
            DietRate.NOT_ADVISED to Rating.Negative,
        )
        val summaries = mapOf(
            (DietRate.GOOD to DietMode.KETO) to R.string.product_verdict_good_keto,
            (DietRate.GOOD to DietMode.LOW_CARB) to R.string.product_verdict_good_low_carb,
            (DietRate.NEUTRAL to DietMode.KETO) to R.string.product_verdict_neutral,
            (DietRate.NEUTRAL to DietMode.LOW_CARB) to R.string.product_verdict_neutral,
            (DietRate.NOT_ADVISED to DietMode.KETO) to R.string.product_verdict_not_advised_keto,
            (DietRate.NOT_ADVISED to DietMode.LOW_CARB) to R.string.product_verdict_not_advised_low_carb,
        )

        summaries.forEach { (key, summary) ->
            val (dietRate, mode) = key
            val verdict = assertIs<VerdictState.Rated>(show(Prototype(analysis = rated(dietRate, mode = mode))).verdict)

            assertEquals(ratings.getValue(dietRate), verdict.rating)
            assertEquals(UiText.Res(summary), verdict.summary)
            assertEquals(
                UiText.Res(
                    R.string.product_advice_portion,
                    UiArg.Numeric.WholeNumber(MAX_PORTION),
                    UiArg.Numeric.Count(DAILY_CARBS.toLong()),
                ),
                verdict.portion,
            )
        }
    }

    @Test
    fun `when there are more than two reasons then only the first two are shown`() {
        val reasons = listOf(
            RatingReason.HighCarbEnergyShare(1.0, DietRate.NOT_ADVISED),
            RatingReason.LowFatAndProteinToCarbRatio(0.5, DietRate.NOT_ADVISED),
            RatingReason.HighSugars(10.6, DietRate.NOT_ADVISED),
        )

        val verdict = assertIs<VerdictState.Rated>(
            show(Prototype(analysis = rated(DietRate.NOT_ADVISED, reasons = reasons))).verdict,
        )

        assertEquals(
            listOf<UiText>(
                UiText.Res(R.string.product_reason_carb_energy_share, UiArg.Numeric.Percent(1.0)),
                UiText.Res(R.string.product_reason_low_fat_and_protein_to_carb_ratio, UiArg.Numeric.Ratio(0.5)),
            ),
            verdict.reasons,
        )
    }

    @Test
    fun `when reasons are mapped then each uses its string and arguments`() {
        val resources = ProductResources()

        assertEquals(
            UiText.Res(R.string.product_reason_high_carbs, UiArg.Numeric.Grams(15.0)),
            resources.reason(RatingReason.HighCarbs(15.0, DietRate.NEUTRAL)),
        )
        assertEquals(
            UiText.Res(R.string.product_reason_high_sugars, UiArg.Numeric.Grams(7.0)),
            resources.reason(RatingReason.HighSugars(7.0, DietRate.NEUTRAL)),
        )
        assertEquals(
            UiText.Res(
                R.string.product_reason_high_carbs_per_serving,
                UiArg.Numeric.Grams(34.98),
                UiArg.Numeric.Grams(330.0),
            ),
            resources.reason(RatingReason.HighCarbsPerServing(330.0, 34.98, DietRate.NOT_ADVISED)),
        )
    }

    @Test
    fun `when there are no reasons then the carbs per 100 g line is shown`() {
        val verdict = assertIs<VerdictState.Rated>(show(Prototype(analysis = rated(DietRate.GOOD))).verdict)

        assertEquals(
            listOf<UiText>(UiText.Res(R.string.product_carbs_per_100g, UiArg.Numeric.Grams(CARBS))),
            verdict.reasons,
        )
    }

    @Test
    fun `when max portion is null then the unlimited text is shown`() {
        val verdict = assertIs<VerdictState.Rated>(
            show(Prototype(analysis = rated(DietRate.GOOD, maxPortion = null))).verdict,
        )

        assertEquals(UiText.Res(R.string.product_advice_unlimited), verdict.portion)
    }

    @Test
    fun `when analysis has insufficient data then verdict is insufficient data`() {
        val content = show(Prototype(analysis = ProductAnalysis.InsufficientData(product())))

        assertEquals(
            VerdictState.InsufficientData(
                summary = UiText.Res(R.string.product_verdict_insufficient_data),
                detail = UiText.Res(R.string.product_verdict_insufficient_data_detail),
                advice = UiText.Res(R.string.product_verdict_insufficient_data_advice),
            ),
            content.verdict,
        )
    }

    @Test
    fun `when brand is blank then brand is null`() {
        val header = show(Prototype(analysis = rated(DietRate.GOOD, product(brand = " ")))).header

        assertNull(header.brand)
    }

    @Test
    fun `when product has name and brand then they are raw texts`() {
        val header = show(
            Prototype(analysis = rated(DietRate.GOOD, product(name = "Nutella", brand = "Ferrero"))),
        ).header

        assertEquals(UiText.Raw("Nutella"), header.name)
        assertEquals(UiText.Raw("Ferrero"), header.brand)
        assertEquals(UiText.Res(R.string.product_image), header.imageDescription)
    }

    @Test
    fun `when product has a known category then header has a chip with its icon and label`() {
        val header = show(Prototype(analysis = rated(DietRate.GOOD, product(category = ProductCategory.WATER)))).header

        assertEquals(
            CategoryChipState(AppIcons.Bottle, UiText.Res(DesignR.string.designsystem_category_water)),
            header.category,
        )
    }

    @Test
    fun `when product category is other then header has no chip`() {
        val header = show(Prototype(analysis = rated(DietRate.GOOD, product(category = ProductCategory.OTHER)))).header

        assertNull(header.category)
    }

    @Test
    fun `when product name is blank then name is unnamed product`() {
        val header = show(Prototype(analysis = rated(DietRate.GOOD, product(name = " ")))).header

        assertEquals(UiText.Res(R.string.product_unnamed), header.name)
    }

    @Test
    fun `when name is collapsed or expanded then max lines are 1 or 10`() {
        val analysis = rated(DietRate.GOOD)

        assertEquals(1, show(Prototype(analysis = analysis)).header.nameMaxLines)
        assertEquals(10, show(Prototype(analysis = analysis, isNameExpanded = true)).header.nameMaxLines)
    }

    @Test
    fun `when image url is null then image is not clickable and preview is hidden even when flagged`() {
        val content = show(
            Prototype(analysis = rated(DietRate.GOOD, product(imageUrl = null)), isImagePreviewOpen = true)
        )

        assertNull(content.header.imageUrl)
        assertFalse(content.header.isImageClickable)
        assertNull(content.imagePreview)
    }

    @Test
    fun `when image url exists then image is clickable and preview follows the flag`() {
        val analysis = rated(DietRate.GOOD, product(imageUrl = IMAGE_URL))

        val closed = show(Prototype(analysis = analysis))
        val open = show(Prototype(analysis = analysis, isImagePreviewOpen = true))

        assertEquals(IMAGE_URL, closed.header.imageUrl)
        assertTrue(closed.header.isImageClickable)
        assertNull(closed.imagePreview)
        assertEquals(ImagePreviewState(IMAGE_URL, UiText.Res(R.string.product_image_preview)), open.imagePreview)
    }

    @Test
    fun `when favourite status is unknown then there is no favourite button`() {
        assertNull(show(Prototype(analysis = rated(DietRate.GOOD), isFavourite = null)).favourite)
    }

    @Test
    fun `when favourite status is known then button shows it and is enabled unless updating`() {
        val analysis = rated(DietRate.GOOD)

        assertEquals(
            FavouriteButtonState(true, UiText.Res(R.string.product_remove_from_favourites), isEnabled = true),
            show(Prototype(analysis = analysis, isFavourite = true)).favourite,
        )
        assertEquals(
            FavouriteButtonState(false, UiText.Res(R.string.product_add_to_favourites), isEnabled = true),
            show(Prototype(analysis = analysis, isFavourite = false)).favourite,
        )
        assertEquals(
            FavouriteButtonState(false, UiText.Res(R.string.product_add_to_favourites), isEnabled = false),
            show(Prototype(analysis = analysis, isFavourite = false, isFavouriteUpdating = true)).favourite,
        )
    }

    @Test
    fun `when serving amount is set or missing then glance serving label matches`() {
        val withServing = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = "15"))))
        val withoutServing = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = null))))
        val blankServing = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = " "))))

        assertEquals(
            UiText.Res(R.string.product_basis_serving_with_value, UiArg.Text("15")),
            withServing.glance.servingLabel,
        )
        assertEquals(UiText.Res(R.string.product_basis_serving), withoutServing.glance.servingLabel)
        assertEquals(UiText.Res(R.string.product_basis_serving), blankServing.glance.servingLabel)
    }

    @Test
    fun `when per 100 g is selected then glance tiles show per 100 g values in order`() {
        val glance = show(Prototype(analysis = rated(DietRate.GOOD, product(nutriments = fullNutriments)))).glance

        assertFalse(glance.isPerServingSelected)
        assertEquals(
            listOf(
                UiText.Res(R.string.product_carbs) to grams(13.0),
                UiText.Res(R.string.product_sugars) to grams(7.0),
                UiText.Res(R.string.product_fibre) to grams(11.0),
                UiText.Res(R.string.product_proteins) to grams(25.0),
                UiText.Res(R.string.product_fats) to grams(50.0),
                UiText.Res(R.string.product_energy) to kcal(588.0),
            ),
            glance.tiles.map { it.label to it.value },
        )
    }

    @Test
    fun `when per serving is selected then glance tiles show per serving values in order`() {
        val glance = show(
            Prototype(
                analysis = rated(DietRate.GOOD, product(nutriments = fullNutriments)),
                isPerServingSelected = true,
            )
        ).glance

        assertTrue(glance.isPerServingSelected)
        assertEquals(
            listOf(
                UiText.Res(R.string.product_carbs) to grams(2.0),
                UiText.Res(R.string.product_sugars) to grams(1.1),
                UiText.Res(R.string.product_fibre) to grams(1.7),
                UiText.Res(R.string.product_proteins) to grams(3.8),
                UiText.Res(R.string.product_fats) to grams(7.5),
                UiText.Res(R.string.product_energy) to kcal(88.0),
            ),
            glance.tiles.map { it.label to it.value },
        )
    }

    @Test
    fun `when glance is shown then only the carbs tile is highlighted with the verdict rating`() {
        val rated = show(Prototype(analysis = rated(DietRate.NOT_ADVISED))).glance
        val insufficient = show(Prototype(analysis = ProductAnalysis.InsufficientData(product()))).glance

        assertEquals(Rating.Negative, rated.tiles.first().highlight)
        assertEquals(Rating.Unknown, insufficient.tiles.first().highlight)
        (rated.tiles.drop(1) + insufficient.tiles.drop(1)).forEach { assertNull(it.highlight) }
    }

    @Test
    fun `when product is shown then data source has the barcode`() {
        val content = show(Prototype(analysis = rated(DietRate.GOOD)))

        assertEquals(UiText.Res(R.string.product_data_source, UiArg.Text("3017620422003")), content.dataSource)
    }

    @Test
    fun `when nutrient values are missing then glance tiles show the missing value text`() {
        val analysis = rated(DietRate.GOOD, product(nutriments = emptyNutriments))
        val per100g = show(Prototype(analysis = analysis)).glance.tiles
        val perServing = show(Prototype(analysis = analysis, isPerServingSelected = true)).glance.tiles

        val missing = UiText.Res(R.string.product_value_missing)
        assertEquals(List(6) { missing }, per100g.map { it.value })
        assertEquals(List(6) { missing }, perServing.map { it.value })
    }

    private fun show(prototype: Prototype): ProductContentState.ShowProduct =
        assertIs<ProductContentState.ShowProduct>(factory.createFor(prototype).content)

    private companion object {
        const val CARBS = 15.0
        const val MAX_PORTION = 266.6
        const val DAILY_CARBS = 40
        const val IMAGE_URL = "https://images.openfoodfacts.org/images/products/301/762/042/2003/front.jpg"

        val emptyNutriments = ProductNutriments(null, null, null, null, null, null, null, null, null, null)
        val fullNutriments = ProductNutriments(
            88.0,
            588.0,
            7.5,
            50.0,
            3.8,
            25.0,
            2.0,
            13.0,
            0.2,
            1.1,
            1.1,
            7.0,
            1.7,
            11.0,
        )

        fun product(
            name: String = "Peanut butter",
            brand: String = "Nuts & Co",
            imageUrl: String? = IMAGE_URL,
            servingAmount: String? = "15",
            nutriments: ProductNutriments = emptyNutriments,
            category: ProductCategory = ProductCategory.OTHER,
        ) = Product(
            name = name,
            barcode = "3017620422003",
            brand = brand,
            nutriments = nutriments,
            imageUrl = imageUrl,
            servingAmount = servingAmount,
            category = category,
        )

        fun rated(
            dietRate: DietRate,
            product: Product = product(),
            mode: DietMode = DietMode.KETO,
            reasons: List<RatingReason> = emptyList(),
            maxPortion: Double? = MAX_PORTION,
        ) = ProductAnalysis.Rated(
            product = product,
            macronutrients = MacronutrientAnalysisResult(mode, dietRate, reasons, CARBS, maxPortion, DAILY_CARBS),
        )

        fun kcal(value: Double) = UiText.Res(R.string.product_value_kcal, UiArg.Numeric.Kcal(value))

        fun grams(value: Double) = UiText.Res(R.string.product_value_grams, UiArg.Numeric.Grams(value))
    }
}
