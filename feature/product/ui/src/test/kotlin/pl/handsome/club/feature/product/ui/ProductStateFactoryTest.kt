package pl.handsome.club.feature.product.ui

import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.designsystem.component.Rating
import pl.handsome.club.core.designsystem.text.UiArg
import pl.handsome.club.core.designsystem.text.UiText
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.analysis.api.model.RatingReason
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.feature.product.ui.state.FavouriteButtonState
import pl.handsome.club.feature.product.ui.state.ImagePreviewState
import pl.handsome.club.feature.product.ui.state.NutrientRowState
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
        val expected = mapOf(
            DietRate.GOOD to (Rating.Positive to R.string.product_rating_good),
            DietRate.NEUTRAL to (Rating.Neutral to R.string.product_rating_neutral),
            DietRate.NOT_ADVISED to (Rating.Negative to R.string.product_rating_not_advised),
        )
        val modes = mapOf(
            DietMode.KETO to R.string.product_diet_mode_keto,
            DietMode.LOW_CARB to R.string.product_diet_mode_low_carb,
        )

        expected.forEach { (dietRate, texts) ->
            modes.forEach { (mode, modeName) ->
                val verdict = assertIs<VerdictState.Rated>(show(Prototype(analysis = rated(dietRate, mode = mode))).verdict)

                assertEquals(texts.first, verdict.rating)
                assertEquals(UiText.Res(texts.second, UiArg.Nested(UiText.Res(modeName))), verdict.summary)
                assertEquals(
                    UiText.Res(
                        R.string.product_maximum_portion_details,
                        UiArg.Numeric.WholeNumber(MAX_PORTION),
                        UiArg.Numeric.Count(DAILY_CARBS.toLong()),
                    ),
                    verdict.portion,
                )
            }
        }
    }

    @Test
    fun `when there are more than two reasons then only the first two are shown`() {
        val reasons = listOf(
            RatingReason.HighCarbEnergyShare(1.0, DietRate.NOT_ADVISED),
            RatingReason.LowFatAndProteinToCarbRatio(0.5, DietRate.NOT_ADVISED),
            RatingReason.HighSugars(10.6, DietRate.NOT_ADVISED),
        )

        val verdict = assertIs<VerdictState.Rated>(show(Prototype(analysis = rated(DietRate.NOT_ADVISED, reasons = reasons))).verdict)

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
            UiText.Res(R.string.product_reason_high_carbs_per_serving, UiArg.Numeric.Grams(34.98), UiArg.Numeric.Grams(330.0)),
            resources.reason(RatingReason.HighCarbsPerServing(330.0, 34.98, DietRate.NOT_ADVISED)),
        )
    }

    @Test
    fun `when there are no reasons then the carbs per 100 g line is shown`() {
        val verdict = assertIs<VerdictState.Rated>(show(Prototype(analysis = rated(DietRate.GOOD))).verdict)

        assertEquals(listOf<UiText>(UiText.Res(R.string.product_carbs_per_100g, UiArg.Numeric.Grams(CARBS))), verdict.reasons)
    }

    @Test
    fun `when max portion is null then the unlimited text is shown`() {
        val verdict = assertIs<VerdictState.Rated>(show(Prototype(analysis = rated(DietRate.GOOD, maxPortion = null))).verdict)

        assertEquals(UiText.Res(R.string.product_maximum_portion_unlimited), verdict.portion)
    }

    @Test
    fun `when analysis has insufficient data then verdict is insufficient data`() {
        val content = show(Prototype(analysis = ProductAnalysis.InsufficientData(product())))

        assertEquals(
            VerdictState.InsufficientData(UiText.Res(R.string.product_rating_insufficient_data)),
            content.verdict,
        )
    }

    @Test
    fun `when product has name and brand then they are raw texts`() {
        val header = show(Prototype(analysis = rated(DietRate.GOOD, product(name = "Nutella", brand = "Ferrero")))).header

        assertEquals(UiText.Raw("Nutella"), header.name)
        assertEquals(UiText.Raw("Ferrero"), header.brand)
        assertEquals(UiText.Res(R.string.product_image), header.imageDescription)
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
    fun `when serving amount is set or missing then serving header matches`() {
        val withServing = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = "15"))))
        val withoutServing = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = null))))
        val blankServing = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = " "))))

        assertEquals(
            UiText.Res(R.string.product_per_serving_with_value, UiArg.Text("15")),
            withServing.nutrients.perServingHeader,
        )
        assertEquals(UiText.Res(R.string.product_per_serving), withoutServing.nutrients.perServingHeader)
        assertEquals(UiText.Res(R.string.product_per_serving), blankServing.nutrients.perServingHeader)
    }

    @Test
    fun `when nutrients are shown then rows keep order and format values`() {
        val rows = show(Prototype(analysis = rated(DietRate.GOOD, product(nutriments = fullNutriments)))).nutrients.rows

        assertEquals(
            listOf(
                NutrientRowState(UiText.Res(R.string.product_energy), kcal(88.0), kcal(588.0)),
                NutrientRowState(UiText.Res(R.string.product_fats), grams(7.5), grams(50.0)),
                NutrientRowState(UiText.Res(R.string.product_proteins), grams(3.8), grams(25.0)),
                NutrientRowState(UiText.Res(R.string.product_carbs), grams(2.0), grams(13.0)),
                NutrientRowState(UiText.Res(R.string.product_sugars), grams(1.1), grams(7.0)),
                NutrientRowState(UiText.Res(R.string.product_fibre), grams(1.7), grams(11.0)),
                NutrientRowState(UiText.Res(R.string.product_salt), grams(0.2), grams(1.1)),
            ),
            rows,
        )
    }

    @Test
    fun `when nutrient values are missing then rows show the missing value text`() {
        val rows = show(Prototype(analysis = rated(DietRate.GOOD, product(nutriments = emptyNutriments)))).nutrients.rows

        val missing = UiText.Res(R.string.product_value_missing)
        rows.forEach { row ->
            assertEquals(missing, row.perServing)
            assertEquals(missing, row.per100g)
        }
        assertEquals(7, rows.size)
    }

    private fun show(prototype: Prototype): ProductContentState.ShowProduct =
        assertIs<ProductContentState.ShowProduct>(factory.createFor(prototype).content)

    private companion object {
        const val CARBS = 15.0
        const val MAX_PORTION = 266.6
        const val DAILY_CARBS = 40
        const val IMAGE_URL = "https://images.openfoodfacts.org/images/products/301/762/042/2003/front.jpg"

        val emptyNutriments = ProductNutriments(null, null, null, null, null, null, null, null, null, null)
        val fullNutriments = ProductNutriments(88.0, 588.0, 7.5, 50.0, 3.8, 25.0, 2.0, 13.0, 0.2, 1.1, 1.1, 7.0, 1.7, 11.0)

        fun product(
            name: String = "Peanut butter",
            brand: String = "Nuts & Co",
            imageUrl: String? = IMAGE_URL,
            servingAmount: String? = "15",
            nutriments: ProductNutriments = emptyNutriments,
        ) = Product(
            name = name,
            barcode = "3017620422003",
            brand = brand,
            nutriments = nutriments,
            imageUrl = imageUrl,
            servingAmount = servingAmount,
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
