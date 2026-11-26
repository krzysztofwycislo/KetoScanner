package app.carbpeek.feature.product.ui

import app.carbpeek.core.common.AppError
import app.carbpeek.core.designsystem.component.Rating
import app.carbpeek.core.designsystem.icon.AppIcons
import app.carbpeek.core.designsystem.text.UiArg
import app.carbpeek.core.designsystem.text.UiText
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.AnalyzedIngredient
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.IngredientName
import app.carbpeek.feature.analysis.api.model.IngredientsAnalysis
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.analysis.api.model.RatingReason
import app.carbpeek.feature.analysis.api.model.Vitamin
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.product.api.model.NovaGroup
import app.carbpeek.feature.product.api.model.NutriScoreGrade
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.feature.product.ui.state.CategoryChipState
import app.carbpeek.feature.product.ui.state.FavouriteButtonState
import app.carbpeek.feature.product.ui.state.ImagePreviewState
import app.carbpeek.feature.product.ui.state.IngredientBackState
import app.carbpeek.feature.product.ui.state.IngredientRowState
import app.carbpeek.feature.product.ui.state.IngredientSectionState
import app.carbpeek.feature.product.ui.state.IngredientSheetState
import app.carbpeek.feature.product.ui.state.IngredientTone
import app.carbpeek.feature.product.ui.state.IngredientsInfoSheetState
import app.carbpeek.feature.product.ui.state.ProductContentState
import app.carbpeek.feature.product.ui.state.ProductStateFactory
import app.carbpeek.feature.product.ui.state.ScoreState
import app.carbpeek.feature.product.ui.state.ScoreTone
import app.carbpeek.feature.product.ui.state.ScoresState
import app.carbpeek.feature.product.ui.state.VerdictState
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import app.carbpeek.core.designsystem.R as DesignR
import app.carbpeek.feature.product.ui.state.ProductStatePrototype as Prototype

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
    fun `when load error is timeout then content is error with timeout message`() {
        val state = factory.createFor(Prototype(loadError = AppError.Timeout))

        assertEquals(
            ProductContentState.Error(UiText.Res(DesignR.string.designsystem_error_timeout)),
            state.content,
        )
    }

    @Test
    fun `when load error is set and analysis exists then content is still error`() {
        val state = factory.createFor(Prototype(analysis = rated(DietRate.GOOD), loadError = AppError.Network))

        assertIs<ProductContentState.Error>(state.content)
    }

    @Test
    fun `when analysis is rated then verdict has rating and summary with mode`() {
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
                UiText.Res(R.string.product_reason_low_fat_and_protein_to_carb_ratio),
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
    fun `when analysis is unrated then there is no verdict and values ingredients and scores stay`() {
        val content = show(
            Prototype(
                analysis = ProductAnalysis.Unrated(
                    product = product(
                        nutriments = fullNutriments,
                        nutriScore = NutriScoreGrade.B,
                        novaGroup = NovaGroup.PROCESSED,
                    ),
                    ingredients = IngredientsAnalysis(
                        ingredients = listOf(ingredient(name = "sugar", kind = IngredientKind.ADDED_SUGAR)),
                    ),
                ),
            ),
        )

        assertNull(content.verdict)
        assertEquals(
            listOf(
                UiText.Res(R.string.product_carbs) to grams(13.0),
                UiText.Res(R.string.product_sugars) to grams(7.0),
                UiText.Res(R.string.product_fibre) to grams(11.0),
                UiText.Res(R.string.product_proteins) to grams(25.0),
                UiText.Res(R.string.product_fats) to grams(50.0),
                UiText.Res(R.string.product_energy) to kcal(588.0),
            ),
            content.macros.values.map { it.label to it.value },
        )
        assertEquals(listOf(UiText.Raw("Sugar")), content.ingredients?.rows?.map { it.label })
        assertEquals(ScoreTone.NUTRI_SCORE_B, content.scores?.nutriScore?.tone)
        assertEquals(ScoreTone.PROCESSING, content.scores?.processing?.tone)
    }

    @Test
    fun `when data is missing without a diet then the insufficient data banner stays`() {
        val content = show(
            Prototype(analysis = ProductAnalysis.InsufficientData(product = product(), hasDiet = false)),
        )

        assertIs<VerdictState.InsufficientData>(content.verdict)
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
    fun `when product has a known category then the placeholder icon is the icon of the category`() {
        val product = product(category = ProductCategory.WATER)
        val header = show(Prototype(analysis = rated(dietRate = DietRate.GOOD, product = product))).header

        assertEquals(AppIcons.Bottle, header.placeholderIcon)
    }

    @Test
    fun `when product category is other then the placeholder icon is the bag and there is no chip`() {
        val product = product(category = ProductCategory.OTHER)
        val header = show(Prototype(analysis = rated(dietRate = DietRate.GOOD, product = product))).header

        assertEquals(AppIcons.Bag, header.placeholderIcon)
        assertNull(header.category)
    }

    @Test
    fun `when product has no photo then the placeholder icon is still the icon of the category`() {
        val product = product(imageUrl = null, category = ProductCategory.WATER)
        val header = show(Prototype(analysis = rated(dietRate = DietRate.GOOD, product = product))).header

        assertNull(header.imageUrl)
        assertEquals(AppIcons.Bottle, header.placeholderIcon)
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
            FavouriteButtonState(
                isFavourite = true,
                description = UiText.Res(R.string.product_remove_from_favourites),
                isEnabled = true,
                icon = AppIcons.HeartFilled,
            ),
            show(Prototype(analysis = analysis, isFavourite = true)).favourite,
        )
        assertEquals(
            FavouriteButtonState(
                isFavourite = false,
                description = UiText.Res(R.string.product_add_to_favourites),
                isEnabled = true,
                icon = AppIcons.Heart,
            ),
            show(Prototype(analysis = analysis, isFavourite = false)).favourite,
        )
        assertEquals(
            FavouriteButtonState(
                isFavourite = false,
                description = UiText.Res(R.string.product_add_to_favourites),
                isEnabled = false,
                icon = AppIcons.Heart,
            ),
            show(Prototype(analysis = analysis, isFavourite = false, isFavouriteUpdating = true)).favourite,
        )
    }

    @Test
    fun `when the product is a favourite then the button shows a filled heart otherwise an outlined one`() {
        val analysis = rated(DietRate.GOOD)

        assertEquals(AppIcons.HeartFilled, show(Prototype(analysis = analysis, isFavourite = true)).favourite?.icon)
        assertEquals(AppIcons.Heart, show(Prototype(analysis = analysis, isFavourite = false)).favourite?.icon)
    }

    @Test
    fun `when serving amount is set or missing then macros serving label matches`() {
        val withServing = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = "15"))))
        val withoutServing = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = null))))
        val blankServing = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = " "))))

        assertEquals(
            UiText.Res(R.string.product_basis_serving_with_value, UiArg.Text("15")),
            withServing.macros.servingLabel,
        )
        assertEquals(UiText.Res(R.string.product_basis_serving), withoutServing.macros.servingLabel)
        assertEquals(UiText.Res(R.string.product_basis_serving), blankServing.macros.servingLabel)
    }

    @Test
    fun `when per 100 g is selected then macros show per 100 g values in order`() {
        val macros = show(Prototype(analysis = rated(DietRate.GOOD, product(nutriments = fullNutriments)))).macros

        assertFalse(macros.isPerServingSelected)
        assertEquals(
            listOf(
                UiText.Res(R.string.product_carbs) to grams(13.0),
                UiText.Res(R.string.product_sugars) to grams(7.0),
                UiText.Res(R.string.product_fibre) to grams(11.0),
                UiText.Res(R.string.product_proteins) to grams(25.0),
                UiText.Res(R.string.product_fats) to grams(50.0),
                UiText.Res(R.string.product_energy) to kcal(588.0),
            ),
            macros.values.map { it.label to it.value },
        )
    }

    @Test
    fun `when per serving is selected then macros show per serving values in order`() {
        val macros = show(
            Prototype(
                analysis = rated(DietRate.GOOD, product(nutriments = fullNutriments)),
                isPerServingSelected = true,
            )
        ).macros

        assertTrue(macros.isPerServingSelected)
        assertEquals(
            listOf(
                UiText.Res(R.string.product_carbs) to grams(2.0),
                UiText.Res(R.string.product_sugars) to grams(1.1),
                UiText.Res(R.string.product_fibre) to grams(1.7),
                UiText.Res(R.string.product_proteins) to grams(3.8),
                UiText.Res(R.string.product_fats) to grams(7.5),
                UiText.Res(R.string.product_energy) to kcal(88.0),
            ),
            macros.values.map { it.label to it.value },
        )
    }

    @Test
    fun `when one carbs reason caused the verdict then only carbs is highlighted`() {
        val ratings = mapOf(
            DietRate.NEUTRAL to Rating.Neutral,
            DietRate.NOT_ADVISED to Rating.Negative,
        )

        ratings.forEach { (rate, rating) ->
            val reasons = listOf(
                RatingReason.HighCarbs(gramsPer100g = 15.0, rate = rate),
                RatingReason.HighCarbEnergyShare(share = 0.6, rate = rate),
                RatingReason.LowFatAndProteinToCarbRatio(ratio = 0.5, rate = rate),
                RatingReason.HighCarbsPerServing(servingGrams = 330.0, carbsGrams = 34.98, rate = rate),
            )
            reasons.forEach { reason ->
                assertEquals(
                    expected = highlights(carbs = rating, sugars = null),
                    actual = highlightsOf(rated(dietRate = rate, reasons = listOf(reason))),
                    message = reason.toString(),
                )
            }
        }
    }

    @Test
    fun `when sugars caused the verdict then only sugars is highlighted`() {
        val analysis = rated(
            dietRate = DietRate.NOT_ADVISED,
            reasons = listOf(RatingReason.HighSugars(gramsPer100g = 13.8, rate = DietRate.NOT_ADVISED)),
        )

        assertEquals(highlights(carbs = null, sugars = Rating.Negative), highlightsOf(analysis))
    }

    @Test
    fun `when carbs and sugars caused the verdict then both are highlighted`() {
        assertEquals(
            highlights(carbs = Rating.Neutral, sugars = Rating.Neutral),
            highlightsOf(ratedForCarbsAndSugars()),
        )
    }

    @Test
    fun `when sugars caused the verdict but the reason is not shown then both are highlighted`() {
        val analysis = rated(
            dietRate = DietRate.NOT_ADVISED,
            reasons = listOf(
                RatingReason.HighCarbs(gramsPer100g = 57.5, rate = DietRate.NOT_ADVISED),
                RatingReason.HighCarbEnergyShare(share = 0.6, rate = DietRate.NOT_ADVISED),
                RatingReason.HighSugars(gramsPer100g = 56.3, rate = DietRate.NOT_ADVISED),
            ),
        )

        assertEquals(highlights(carbs = Rating.Negative, sugars = Rating.Negative), highlightsOf(analysis))
    }

    @Test
    fun `when the carbs reason is weaker than the verdict then only sugars is highlighted`() {
        val analysis = rated(
            dietRate = DietRate.NOT_ADVISED,
            reasons = listOf(
                RatingReason.HighSugars(gramsPer100g = 13.8, rate = DietRate.NOT_ADVISED),
                RatingReason.HighCarbs(gramsPer100g = 14.1, rate = DietRate.NEUTRAL),
            ),
        )

        assertEquals(highlights(carbs = null, sugars = Rating.Negative), highlightsOf(analysis))
    }

    @Test
    fun `when the sugars reason is weaker than the verdict then only carbs is highlighted`() {
        val analysis = rated(
            dietRate = DietRate.NOT_ADVISED,
            reasons = listOf(
                RatingReason.HighCarbs(gramsPer100g = 57.5, rate = DietRate.NOT_ADVISED),
                RatingReason.HighSugars(gramsPer100g = 7.0, rate = DietRate.NEUTRAL),
            ),
        )

        assertEquals(highlights(carbs = Rating.Negative, sugars = null), highlightsOf(analysis))
    }

    @Test
    fun `when the verdict is good then only carbs is highlighted`() {
        assertEquals(
            highlights(carbs = Rating.Positive, sugars = null),
            highlightsOf(rated(DietRate.GOOD)),
        )
    }

    @Test
    fun `when analysis has insufficient data then no value is highlighted`() {
        assertEquals(
            highlights(carbs = null, sugars = null),
            highlightsOf(ProductAnalysis.InsufficientData(product())),
        )
    }

    @Test
    fun `when analysis is unrated then no value is highlighted`() {
        assertEquals(
            highlights(carbs = null, sugars = null),
            highlightsOf(ProductAnalysis.Unrated(product(nutriments = fullNutriments))),
        )
    }

    @Test
    fun `when the serving is switched then the highlights stay the same`() {
        val analysis = ratedForCarbsAndSugars()
        val per100g = show(Prototype(analysis = analysis, isPerServingSelected = false)).macros
        val perServing = show(Prototype(analysis = analysis, isPerServingSelected = true)).macros

        assertFalse(per100g.isPerServingSelected)
        assertTrue(perServing.isPerServingSelected)
        assertEquals(
            highlights(carbs = Rating.Neutral, sugars = Rating.Neutral),
            per100g.values.map { it.highlight },
        )
        assertEquals(per100g.values.map { it.highlight }, perServing.values.map { it.highlight })
    }

    @Test
    fun `when product is shown then data source has the barcode`() {
        val content = show(Prototype(analysis = rated(DietRate.GOOD)))

        assertEquals(UiText.Res(R.string.product_data_source, UiArg.Text("3017620422003")), content.dataSource)
    }

    @Test
    fun `when nutrient values are missing then macros show the missing value text`() {
        val analysis = rated(DietRate.GOOD, product(nutriments = emptyNutriments))
        val per100g = show(Prototype(analysis = analysis)).macros.values
        val perServing = show(Prototype(analysis = analysis, isPerServingSelected = true)).macros.values

        val missing = UiText.Res(R.string.product_value_missing)
        assertEquals(List(6) { missing }, per100g.map { it.value })
        assertEquals(List(6) { missing }, perServing.map { it.value })
    }

    @Test
    fun `when the serving is usable then the serving toggle is visible`() {
        val macros = show(Prototype(analysis = rated(DietRate.GOOD, product(servingAmount = "250")))).macros

        assertTrue(macros.isServingToggleVisible)
    }

    @Test
    fun `when the serving is over 500 then the toggle is hidden and per 100 g values are shown`() {
        val macros = show(
            Prototype(
                analysis = rated(
                    dietRate = DietRate.GOOD,
                    product = product(servingAmount = "1000", nutriments = fullNutriments),
                ),
                isPerServingSelected = true,
            )
        ).macros

        assertFalse(macros.isServingToggleVisible)
        assertFalse(macros.isPerServingSelected)
        assertEquals(grams(13.0), macros.values.first().value)
    }

    @Test
    fun `when there is no serving then the serving toggle is hidden`() {
        val macros = show(
            Prototype(
                analysis = rated(dietRate = DietRate.GOOD, product = product(servingAmount = null)),
                isPerServingSelected = true,
            )
        ).macros

        assertFalse(macros.isServingToggleVisible)
        assertFalse(macros.isPerServingSelected)
    }

    @Test
    fun `when the serving is exactly 100 then the toggle is hidden and per 100 g values are shown`() {
        val macros = show(
            Prototype(
                analysis = rated(
                    dietRate = DietRate.GOOD,
                    product = product(servingAmount = "100", nutriments = fullNutriments),
                ),
                isPerServingSelected = true,
            )
        ).macros

        assertFalse(macros.isServingToggleVisible)
        assertFalse(macros.isPerServingSelected)
        assertEquals(grams(13.0), macros.values.first().value)
    }

    @Test
    fun `when the product has no scores then there is no score row`() {
        val scores = show(Prototype(analysis = rated(DietRate.GOOD))).scores

        assertNull(scores)
    }

    @Test
    fun `when only a nutri score exists then only its score is shown`() {
        val scores = scoresOf(product(nutriScore = NutriScoreGrade.B))

        assertEquals(
            ScoreState(
                badge = UiText.Raw("B"),
                title = UiText.Res(R.string.product_nutri_score),
                note = UiText.Res(R.string.product_nutri_score_b),
                tone = ScoreTone.NUTRI_SCORE_B,
            ),
            scores.nutriScore,
        )
        assertNull(scores.processing)
    }

    @Test
    fun `when only a nova group exists then only the processing score is shown`() {
        val scores = scoresOf(product(novaGroup = NovaGroup.PROCESSED))

        assertNull(scores.nutriScore)
        assertEquals(
            ScoreState(
                badge = UiText.Res(R.string.product_processing_badge, UiArg.Numeric.WholeNumber(3.0)),
                title = UiText.Res(R.string.product_processing),
                note = UiText.Res(R.string.product_processing_3),
                tone = ScoreTone.PROCESSING,
            ),
            scores.processing,
        )
    }

    @Test
    fun `when there is a diet then the scores are other ratings with a note about the diet`() {
        val product = product(nutriScore = NutriScoreGrade.B)

        listOf(
            rated(dietRate = DietRate.GOOD, product = product),
            ProductAnalysis.InsufficientData(product),
        ).forEach { analysis ->
            val scores = assertNotNull(show(Prototype(analysis = analysis)).scores)

            assertEquals(UiText.Res(R.string.product_other_ratings), scores.title)
            assertEquals(UiText.Res(R.string.product_other_ratings_note), scores.note)
        }
    }

    @Test
    fun `when there is no diet then the scores are ratings without a note`() {
        val product = product(nutriScore = NutriScoreGrade.B)

        listOf(
            ProductAnalysis.Unrated(product),
            ProductAnalysis.InsufficientData(product = product, hasDiet = false),
        ).forEach { analysis ->
            val scores = assertNotNull(show(Prototype(analysis = analysis)).scores)

            assertEquals(UiText.Res(R.string.product_ratings), scores.title)
            assertNull(scores.note)
        }
    }

    @Test
    fun `every nutri score grade has its badge note and tone`() {
        val expected = mapOf(
            NutriScoreGrade.A to (R.string.product_nutri_score_a to ScoreTone.NUTRI_SCORE_A),
            NutriScoreGrade.B to (R.string.product_nutri_score_b to ScoreTone.NUTRI_SCORE_B),
            NutriScoreGrade.C to (R.string.product_nutri_score_c to ScoreTone.NUTRI_SCORE_C),
            NutriScoreGrade.D to (R.string.product_nutri_score_d to ScoreTone.NUTRI_SCORE_D),
            NutriScoreGrade.E to (R.string.product_nutri_score_e to ScoreTone.NUTRI_SCORE_E),
        )

        expected.forEach { (grade, noteAndTone) ->
            val scores = scoresOf(product(nutriScore = grade, novaGroup = NovaGroup.ULTRA_PROCESSED))
            val score = assertNotNull(scores.nutriScore)

            assertEquals(UiText.Raw(grade.name), score.badge, "$grade")
            assertEquals(UiText.Res(noteAndTone.first), score.note, "$grade")
            assertEquals(noteAndTone.second, score.tone, "$grade")
            assertNotNull(scores.processing)
        }
    }

    @Test
    fun `every nova group has its number and note`() {
        val expected = mapOf(
            NovaGroup.UNPROCESSED to R.string.product_processing_1,
            NovaGroup.CULINARY_INGREDIENT to R.string.product_processing_2,
            NovaGroup.PROCESSED to R.string.product_processing_3,
            NovaGroup.ULTRA_PROCESSED to R.string.product_processing_4,
        )

        expected.forEach { (group, note) ->
            val scores = scoresOf(product(nutriScore = NutriScoreGrade.A, novaGroup = group))
            val score = assertNotNull(scores.processing)

            assertEquals(
                UiText.Res(R.string.product_processing_badge, UiArg.Numeric.WholeNumber(group.number.toDouble())),
                score.badge,
                "$group",
            )
            assertEquals(UiText.Res(note), score.note, "$group")
            assertEquals(ScoreTone.PROCESSING, score.tone, "$group")
            assertNotNull(scores.nutriScore)
        }
    }

    @Test
    fun `when the analysis has no ingredients then no ingredients block or sheet is shown`() {
        val content = show(Prototype(analysis = rated(DietRate.GOOD), openIngredientIndex = 0))

        assertNull(content.ingredients)
        assertNull(content.ingredientSheet)
    }

    @Test
    fun `when an analysis with insufficient data has ingredients then the card is shown`() {
        val sugar = ingredient(name = "sugar", kind = IngredientKind.ADDED_SUGAR)
        val analysis = ProductAnalysis.InsufficientData(
            product = product(),
            ingredients = IngredientsAnalysis(ingredients = listOf(sugar)),
        )

        val content = show(Prototype(analysis = analysis))

        assertEquals(
            listOf(UiText.Raw("Sugar")),
            content.ingredients?.rows?.map { it.label },
        )
    }

    @Test
    fun `when a name is shown then it is upper cased in the row and the sheet title`() {
        val stevia = ingredient(name = "stevia", id = "en:e960", kind = IngredientKind.SWEETENER)
        val content = show(
            Prototype(
                analysis = withIngredients(listOf(stevia)),
                openIngredientIndex = 0,
            ),
        )

        assertEquals(UiText.Raw("Stevia"), content.ingredients?.rows?.single()?.label)
        assertEquals(UiText.Raw("Stevia"), content.ingredientSheet?.title)
    }

    @Test
    fun `every ingredient kind maps to its sheet tone and the rows keep label order`() {
        val tones = mapOf(
            IngredientKind.ADDED_SUGAR to IngredientTone.ADDED_SUGAR,
            IngredientKind.POLYOL to IngredientTone.SWEETENER,
            IngredientKind.SWEETENER to IngredientTone.SWEETENER,
            IngredientKind.STARCH to IngredientTone.STARCH,
            IngredientKind.MILK to IngredientTone.MILK,
            IngredientKind.ADDITIVE to IngredientTone.ADDITIVE,
            IngredientKind.PLAIN to IngredientTone.PLAIN,
        )
        val ingredients = tones.keys.map { kind ->
            ingredient(name = kind.name.lowercase(), kind = kind, contains = listOf(ingredient(name = "water")))
        }

        val rows = cardRows(ingredients)

        assertEquals(tones.values.toList(), ingredients.map { sheet(it).tone })
        assertEquals(
            ingredients.map { UiText.Raw(it.name.name.replaceFirstChar { char -> char.uppercase() }) },
            rows.map { it.label },
        )
    }

    @Test
    fun `when an ingredient declares a percent then the row label and description include it`() {
        val sugar = ingredient(
            name = "sugar",
            id = "en:sugar",
            kind = IngredientKind.ADDED_SUGAR,
            declaredPercent = 8.7,
        )
        val salt = ingredient(name = "salt")

        val rows = assertNotNull(
            show(
                Prototype(
                    analysis = withIngredients(listOf(sugar, salt)),
                ),
            ).ingredients,
        ).rows

        val sugarLabel = UiText.Res(
            R.string.product_ingredient_with_percent,
            UiArg.Nested(UiText.Raw("Sugar")),
            UiArg.Numeric.LabelPercent(0.087),
        )
        assertEquals(
            IngredientRowState(
                label = sugarLabel,
                count = null,
                description = UiText.Res(
                    R.string.product_ingredient_description,
                    UiArg.Nested(sugarLabel),
                    UiArg.Nested(UiText.Res(R.string.product_ingredient_tag_added_sugar)),
                ),
                isOpenable = true,
            ),
            rows[0],
        )
        assertEquals(
            IngredientRowState(
                label = UiText.Raw("Salt"),
                count = null,
                description = UiText.Raw("Salt"),
                isOpenable = false,
            ),
            rows[1],
        )
    }

    @Test
    fun `the ingredients info sheet is closed by default`() {
        val analysis = withIngredients(listOf(ingredient(name = "milk")))

        assertNull(show(Prototype(analysis = analysis)).ingredientsInfoSheet)
    }

    @Test
    fun `when the product has no ingredients then there is no ingredients info sheet`() {
        val analysis = withIngredients(listOf(ingredient(name = "milk"))).copy(ingredients = null)

        assertNull(
            show(Prototype(analysis = analysis, isIngredientsInfoOpen = true)).ingredientsInfoSheet,
        )
    }

    @Test
    fun `when no ingredient is open or the index is out of range then there is no sheet`() {
        val analysis = withIngredients(listOf(ingredient(name = "sugar", id = "en:sugar")))

        assertNull(show(Prototype(analysis = analysis)).ingredientSheet)
        assertNull(show(Prototype(analysis = analysis, openIngredientIndex = 1)).ingredientSheet)
        assertNull(show(Prototype(analysis = analysis, openIngredientIndex = -1)).ingredientSheet)
    }

    @Test
    fun `when the ingredients info is open then its sheet is shown`() {
        val analysis = withIngredients(listOf(ingredient(name = "milk")))

        assertEquals(
            IngredientsInfoSheetState,
            show(Prototype(analysis = analysis, isIngredientsInfoOpen = true)).ingredientsInfoSheet,
        )
    }

    @Test
    fun `when an ingredient with both texts is open then the sheet shows them in place of the type text`() {
        val sugar = ingredient(
            name = "sugar",
            id = "en:sugar",
            labelName = "cukier",
            kind = IngredientKind.ADDED_SUGAR,
        )

        assertEquals(
            IngredientSheetState(
                back = null,
                tag = UiText.Res(R.string.product_ingredient_tag_added_sugar),
                tone = IngredientTone.ADDED_SUGAR,
                title = UiText.Raw("Sugar"),
                labelName = onTheLabel("cukier"),
                contents = persistentListOf(),
                sections = sugarSections,
            ),
            sheet(sugar),
        )
    }

    @Test
    fun `when a text has only what it is then nothing else is shown`() {
        val cocoaButter = ingredient(name = "cocoa butter", id = "en:cocoa-butter", kind = IngredientKind.SWEETENER)

        assertEquals(
            listOf(what(R.string.product_ingredient_about_fat_cocoa_beans)),
            sheet(cocoaButter).sections.toList(),
        )
    }

    @Test
    fun `when a text has only what it does then the type text is not shown`() {
        val sweetener = ingredient(name = "sweetener", id = "en:sweetener", kind = IngredientKind.SWEETENER)

        assertEquals(
            listOf(does(R.string.product_ingredient_about_adds_sweetness)),
            sheet(sweetener).sections.toList(),
        )
    }

    @Test
    fun `when riboflavin is a vitamin then it has no text otherwise it has one`() {
        listOf("en:e101", "en:e101i").forEach { id ->
            val colour = ingredient(name = "E101", id = id, eNumber = "E101", kind = IngredientKind.ADDITIVE)
            val vitamin = ingredient(name = "E101", id = id, labelName = "ryboflawina", vitamin = Vitamin.RIBOFLAVIN)

            assertEquals(
                listOf(
                    what(R.string.product_ingredient_about_vitamin_b2),
                    does(R.string.product_ingredient_about_colours_food),
                ),
                sheet(colour).sections.toList(),
                id,
            )
            assertEquals(listOf(true, false), cardRows(listOf(colour, vitamin)).map { it.isOpenable }, id)
            assertNull(sheetOrNull(vitamin), id)
        }
    }

    @Test
    fun `when niacin is listed then it has no text`() {
        val additive = ingredient(name = "E375", id = "en:e375", eNumber = "E375")
        val vitamin = ingredient(name = "E375", id = "en:e375", vitamin = Vitamin.NIACIN)

        assertNull(sheetOrNull(additive))
        assertNull(sheetOrNull(vitamin))
    }

    @Test
    fun `when an additive waits for its safety note then it has no text`() {
        listOf("en:e250", "en:e223").forEach { id ->
            assertNull(sheetOrNull(ingredient(name = id, id = id, kind = IngredientKind.ADDITIVE)), id)
        }
    }

    @Test
    fun `when an id only starts like one with a text then it has no text`() {
        listOf("en:sugars", "en:e322i", "fr:sugar", "sugar").forEach { id ->
            assertNull(sheetOrNull(ingredient(name = id, id = id, kind = IngredientKind.ADDED_SUGAR)), id)
        }
    }

    @Test
    fun `rows with a text or contents open and all others are plain`() {
        val garlic = ingredient(name = "garlic", id = "en:garlic")
        val sugar = ingredient(name = "sugar", id = "en:sugar", kind = IngredientKind.ADDED_SUGAR)
        val lysozyme = ingredient(name = "E1105", id = "en:e1105", eNumber = "E1105", kind = IngredientKind.ADDITIVE)
        val coating = ingredient(name = "coating", id = "en:coating", contains = listOf(garlic, sugar, lysozyme))
        val cocoa = ingredient(name = "cocoa", id = "en:cocoa", contains = listOf(garlic, sugar))

        assertEquals(
            listOf(false, true, false, true, true),
            cardRows(listOf(garlic, sugar, lysozyme, coating, cocoa)).map { it.isOpenable },
        )
        assertEquals(listOf(false, true, false), sheet(coating).contents.map { it.isOpenable })
    }

    @Test
    fun `when a compound has no text of its own then its type text is shown`() {
        val does = does(R.string.product_ingredient_does_sweetener)
        val sections = mapOf(
            IngredientKind.ADDED_SUGAR to listOf(what(R.string.product_ingredient_what_added_sugar)),
            IngredientKind.POLYOL to listOf(what(R.string.product_ingredient_what_polyol), does),
            IngredientKind.SWEETENER to listOf(what(R.string.product_ingredient_what_sweetener), does),
            IngredientKind.STARCH to listOf(what(R.string.product_ingredient_what_starch)),
            IngredientKind.MILK to listOf(what(R.string.product_ingredient_what_milk)),
            IngredientKind.ADDITIVE to listOf(what(R.string.product_ingredient_what_additive)),
            IngredientKind.PLAIN to emptyList(),
        )

        assertEquals(IngredientKind.entries.toSet(), sections.keys)
        sections.forEach { (kind, expected) ->
            val compound = ingredient(name = "name", kind = kind, contains = twoContents)

            assertEquals(expected, sheet(compound).sections.toList(), "$kind")
        }
    }

    @Test
    fun `polyols and sweeteners share one tag and regular ingredients have none`() {
        val sweetener = UiText.Res(R.string.product_ingredient_tag_sweetener)

        assertEquals(
            sweetener,
            sheet(ingredient(name = "maltitol", id = "en:e965", kind = IngredientKind.POLYOL)).tag,
        )
        assertEquals(
            sweetener,
            sheet(ingredient(name = "stevia", id = "en:e960", kind = IngredientKind.SWEETENER)).tag,
        )
        assertNull(sheet(ingredient(name = "yeast", id = "en:yeast")).tag)
    }

    @Test
    fun `when an ingredient without a text and contents is open then there is no sheet`() {
        IngredientKind.entries.forEach { kind ->
            assertNull(sheetOrNull(ingredient(name = "name", kind = kind)), "$kind")
        }
    }

    @Test
    fun `when a compound is open then its contents are the rows of the list in order without a count`() {
        val contents = listOf(
            ingredient(name = "wheat flour", kind = IngredientKind.STARCH, declaredPercent = 12.5),
            ingredient(name = "water"),
            ingredient(name = "E965", labelName = "maltitol", eNumber = "E965", kind = IngredientKind.POLYOL),
            ingredient(name = "E322", labelName = "lecytyny", eNumber = "E322", kind = IngredientKind.ADDITIVE),
            ingredient(name = "E375", labelName = "niacyna", vitamin = Vitamin.NIACIN),
        )

        val rows = sheet(ingredient(name = "coating", contains = contents)).contents.toList()

        assertEquals(cardRows(contents), rows)
        assertEquals(
            listOf(
                UiText.Res(
                    R.string.product_ingredient_with_percent,
                    UiArg.Nested(UiText.Raw("Wheat flour")),
                    UiArg.Numeric.LabelPercent(0.125),
                ),
                UiText.Raw("Water"),
                knownName(id = R.string.product_ingredient_e965, eNumber = "E965"),
                withENumber(name = UiArg.Text("Lecytyny"), eNumber = "E322"),
                UiText.Res(R.string.product_ingredient_vitamin_b3),
            ),
            rows.map { it.label },
        )
        assertTrue(rows.all { it.count == null })
    }

    @Test
    fun `when an ingredient has contents then its row shows and speaks the count after the type`() {
        val contents = listOf(ingredient(name = "wheat flour"), ingredient(name = "salt"))

        val (typed, untyped, single) = cardRows(
            listOf(
                ingredient(
                    name = "sourdough",
                    kind = IngredientKind.STARCH,
                    declaredPercent = 20.0,
                    contains = contents,
                ),
                ingredient(name = "coating", contains = contents + ingredient(name = "water")),
                ingredient(name = "pepper"),
            ),
        )

        val sourdough = UiText.Res(
            R.string.product_ingredient_with_percent,
            UiArg.Nested(UiText.Raw("Sourdough")),
            UiArg.Numeric.LabelPercent(0.2),
        )
        assertEquals(sourdough, typed.label)
        assertEquals(inside(2), typed.count)
        assertEquals(
            spoken(
                text = spoken(text = sourdough, detail = UiText.Res(R.string.product_ingredient_tag_starch)),
                detail = inside(2),
            ),
            typed.description,
        )
        assertEquals(inside(3), untyped.count)
        assertEquals(spoken(text = UiText.Raw("Coating"), detail = inside(3)), untyped.description)
        assertNull(single.count)
        assertEquals(UiText.Raw("Pepper"), single.description)
    }

    @Test
    fun `when a compound holds added sugar then its sheet keeps its own type`() {
        val sugar = ingredient(name = "sugar", kind = IngredientKind.ADDED_SUGAR)
        val mustard = ingredient(name = "mustard", contains = listOf(ingredient(name = "water"), sugar))
        val breadcrumbs = ingredient(
            name = "breadcrumbs",
            kind = IngredientKind.STARCH,
            contains = listOf(ingredient(name = "salt"), sugar),
        )

        assertEquals(IngredientTone.PLAIN, sheet(mustard).tone)
        assertNull(sheet(mustard).tag)
        assertEquals(IngredientTone.STARCH, sheet(breadcrumbs).tone)
        assertEquals(UiText.Res(R.string.product_ingredient_tag_starch), sheet(breadcrumbs).tag)
    }

    @Test
    fun `when a typed compound is open then its contents and its type text are shown`() {
        val contents = listOf(ingredient(name = "flour"), ingredient(name = "salt"))
        val flour = ingredient(name = "wheat flour", kind = IngredientKind.STARCH, contains = contents)

        val sheet = sheet(flour)

        assertEquals(cardRows(contents), sheet.contents.toList())
        assertEquals(listOf(what(R.string.product_ingredient_what_starch)), sheet.sections.toList())
    }

    @Test
    fun `when a regular compound is open then only its contents are shown`() {
        val mustard = ingredient(
            name = "mustard",
            contains = listOf(ingredient(name = "water"), ingredient(name = "salt")),
        )

        val sheet = sheet(mustard)

        assertEquals(2, sheet.contents.size)
        assertTrue(sheet.sections.isEmpty())
    }

    @Test
    fun `when a compound has a text of its own then its contents and its text are shown`() {
        val cocoa = ingredient(name = "cocoa", id = "en:cocoa", kind = IngredientKind.STARCH, contains = twoContents)

        val sheet = sheet(cocoa)

        assertEquals(cardRows(twoContents), sheet.contents.toList())
        assertEquals(
            listOf(what(R.string.product_ingredient_about_roasted_cocoa_beans_ground)),
            sheet.sections.toList(),
        )
    }

    @Test
    fun `when a class has contents then its contents and its text are shown`() {
        val raisingAgents = ingredient(
            name = "raising agent",
            id = "en:raising-agent",
            contains = listOf(
                ingredient(name = "E500", id = "en:e500", eNumber = "E500", kind = IngredientKind.ADDITIVE),
                ingredient(name = "E503", id = "en:e503", eNumber = "E503", kind = IngredientKind.ADDITIVE),
            ),
        )

        val sheet = sheet(raisingAgents)

        assertEquals(2, sheet.contents.size)
        assertEquals(
            listOf(does(R.string.product_ingredient_about_gives_off_gas_makes)),
            sheet.sections.toList(),
        )
    }

    @Test
    fun `when a typed content is open then its own details and a way back to the compound are shown`() {
        val coating = ingredient(
            name = "coating",
            contains = listOf(
                ingredient(name = "water"),
                ingredient(
                    name = "sugar",
                    id = "en:sugar",
                    labelName = "sucre",
                    kind = IngredientKind.ADDED_SUGAR,
                ),
            ),
        )

        assertEquals(
            IngredientSheetState(
                back = IngredientBackState(
                    label = UiText.Raw("Coating"),
                    description = UiText.Res(
                        R.string.product_ingredient_back,
                        UiArg.Nested(UiText.Raw("Coating")),
                    ),
                ),
                tag = UiText.Res(R.string.product_ingredient_tag_added_sugar),
                tone = IngredientTone.ADDED_SUGAR,
                title = UiText.Raw("Sugar"),
                labelName = onTheLabel("sucre"),
                contents = persistentListOf(),
                sections = sugarSections,
            ),
            sheet(ingredient = coating, contentIndex = 1),
        )
    }

    @Test
    fun `when the open content has no text then its compound is shown without a way back`() {
        val coating = ingredient(
            name = "coating",
            contains = listOf(ingredient(name = "water"), ingredient(name = "sugar", id = "en:sugar")),
        )

        assertEquals(sheet(coating), sheet(ingredient = coating, contentIndex = 0))
        assertNull(sheet(ingredient = coating, contentIndex = 0).back)
        assertNotNull(sheet(ingredient = coating, contentIndex = 1).back)
    }

    @Test
    fun `when the open content does not exist then the ingredient is shown without a way back`() {
        val coating = ingredient(
            name = "coating",
            contains = listOf(ingredient(name = "water"), ingredient(name = "salt")),
        )
        val vanillin = ingredient(name = "vanillin", id = "en:vanillin")

        assertEquals(sheet(coating), sheet(ingredient = coating, contentIndex = 2))
        assertEquals(sheet(coating), sheet(ingredient = coating, contentIndex = -1))
        assertEquals(sheet(vanillin), sheet(ingredient = vanillin, contentIndex = 0))
        assertNull(sheet(coating).back)
        assertNull(sheet(vanillin).back)
    }

    @Test
    fun `when a content is open without an open ingredient then there is no sheet`() {
        val coating = ingredient(
            name = "coating",
            contains = listOf(ingredient(name = "water"), ingredient(name = "salt")),
        )

        val content = show(
            Prototype(
                analysis = withIngredients(listOf(coating)),
                openIngredientContentIndex = 0,
            ),
        )

        assertNull(content.ingredientSheet)
    }

    @Test
    fun `a polyol or sweetener with an e number shows its English name and the label name when translated`() {
        val erythritol = ingredient(name = "E968", id = "en:e968", labelName = "eritritol", eNumber = "E968")
        val stevia = ingredient(name = "E960a", id = "en:e960a", labelName = "E 960a", eNumber = "E960a")
        val english = ingredient(name = "xylitol", id = "en:e967", eNumber = "E967")

        assertEquals(knownName(id = R.string.product_ingredient_e968, eNumber = "E968"), sheet(erythritol).title)
        assertEquals(onTheLabel("eritritol"), sheet(erythritol).labelName)
        assertEquals(knownName(id = R.string.product_ingredient_e960, eNumber = "E960a"), sheet(stevia).title)
        assertNull(sheet(stevia).labelName)
        assertEquals(knownName(id = R.string.product_ingredient_e967, eNumber = "E967"), sheet(english).title)
        assertNull(sheet(english).labelName)
    }

    @Test
    fun `every polyol and sweetener number has its name`() {
        val names = mapOf(
            "E420" to R.string.product_ingredient_e420,
            "E421" to R.string.product_ingredient_e421,
            "E950" to R.string.product_ingredient_e950,
            "E951" to R.string.product_ingredient_e951,
            "E952" to R.string.product_ingredient_e952,
            "E953" to R.string.product_ingredient_e953,
            "E954" to R.string.product_ingredient_e954,
            "E955" to R.string.product_ingredient_e955,
            "E957" to R.string.product_ingredient_e957,
            "E959" to R.string.product_ingredient_e959,
            "E960" to R.string.product_ingredient_e960,
            "E961" to R.string.product_ingredient_e961,
            "E962" to R.string.product_ingredient_e962,
            "E964" to R.string.product_ingredient_e964,
            "E965" to R.string.product_ingredient_e965,
            "E966" to R.string.product_ingredient_e966,
            "E967" to R.string.product_ingredient_e967,
            "E968" to R.string.product_ingredient_e968,
            "E969" to R.string.product_ingredient_e969,
        )

        names.forEach { (eNumber, id) ->
            assertEquals(
                knownName(id = id, eNumber = eNumber),
                cardRows(listOf(ingredient(name = eNumber, eNumber = eNumber))).single().label,
                eNumber,
            )
        }
    }

    @Test
    fun `another additive shows its label name with the e number and no label line`() {
        val lecithins = ingredient(name = "E322", id = "en:e322", labelName = "lecytyny", eNumber = "E322")

        assertEquals(withENumber(name = UiArg.Text("Lecytyny"), eNumber = "E322"), sheet(lecithins).title)
        assertNull(sheet(lecithins).labelName)
    }

    @Test
    fun `when the label prints only a code then the e number is shown`() {
        listOf("E 471", "e471", "E-471", "E471").forEach { labelName ->
            val additive = ingredient(name = "E471", id = "en:e471", labelName = labelName, eNumber = "E471")

            assertEquals(UiText.Raw("E471"), sheet(additive).title, labelName)
            assertNull(sheet(additive).labelName, labelName)
        }
        assertEquals(
            UiText.Raw("E500ii"),
            sheet(
                ingredient(
                    name = "E500ii",
                    id = "en:e500ii",
                    labelName = "E500(ii)",
                    eNumber = "E500ii",
                ),
            ).title,
        )
    }

    @Test
    fun `when the label name already holds the code then the code is not repeated`() {
        assertEquals(
            UiText.Raw("Emulgator E 471"),
            sheet(
                ingredient(
                    name = "E471",
                    id = "en:e471",
                    labelName = "emulgator E 471",
                    eNumber = "E471",
                ),
            ).title,
        )
        assertEquals(
            UiText.Raw("Sodium carbonates e-500(ii)"),
            sheet(
                ingredient(
                    name = "E500ii",
                    id = "en:e500ii",
                    labelName = "sodium carbonates e-500(ii)",
                    eNumber = "E500ii",
                ),
            ).title,
        )
    }

    @Test
    fun `the label line is hidden when the label name differs only in case`() {
        assertNull(sheet(ingredient(name = "sugar", id = "en:sugar", labelName = "SUGAR")).labelName)
    }

    @Test
    fun `a vitamin with contents shows its English name without an e number and the label name`() {
        val translated = sheet(
            ingredient(name = "E375", labelName = "niacyna", vitamin = Vitamin.NIACIN, contains = twoContents),
        )
        val english = sheet(
            ingredient(name = "riboflavin", vitamin = Vitamin.RIBOFLAVIN, contains = twoContents),
        )
        val coded = sheet(ingredient(name = "E375", vitamin = Vitamin.NIACIN, contains = twoContents))

        assertNull(translated.tag)
        assertEquals(UiText.Res(R.string.product_ingredient_vitamin_b3), translated.title)
        assertEquals(onTheLabel("niacyna"), translated.labelName)
        assertTrue(translated.sections.isEmpty())
        assertEquals(UiText.Res(R.string.product_ingredient_vitamin_b2), english.title)
        assertEquals(onTheLabel("riboflavin"), english.labelName)
        assertEquals(UiText.Res(R.string.product_ingredient_vitamin_b3), coded.title)
        assertEquals(onTheLabel("E375"), coded.labelName)
    }

    @Test
    fun `a vitamin row and its description are the vitamin name without a type`() {
        val analysis = withIngredients(
            listOf(
                ingredient(name = "E375", labelName = "niacyna", vitamin = Vitamin.NIACIN),
                ingredient(
                    name = "E101",
                    labelName = "ryboflawina",
                    vitamin = Vitamin.RIBOFLAVIN,
                    declaredPercent = 0.2,
                ),
            ),
        )

        val rows = assertNotNull(show(Prototype(analysis = analysis)).ingredients).rows

        val labels = listOf(
            UiText.Res(R.string.product_ingredient_vitamin_b3),
            UiText.Res(
                R.string.product_ingredient_with_percent,
                UiArg.Nested(UiText.Res(R.string.product_ingredient_vitamin_b2)),
                UiArg.Numeric.LabelPercent(0.002),
            ),
        )
        assertEquals(
            labels.map { label ->
                IngredientRowState(
                    label = label,
                    count = null,
                    description = label,
                    isOpenable = false,
                )
            },
            rows.toList(),
        )
    }

    private fun sheet(ingredient: AnalyzedIngredient, contentIndex: Int? = null): IngredientSheetState =
        assertNotNull(sheetOrNull(ingredient = ingredient, contentIndex = contentIndex))

    private fun sheetOrNull(ingredient: AnalyzedIngredient, contentIndex: Int? = null): IngredientSheetState? = show(
        Prototype(
            analysis = withIngredients(listOf(ingredient)),
            openIngredientIndex = 0,
            openIngredientContentIndex = contentIndex,
        ),
    ).ingredientSheet

    private fun cardRows(ingredients: List<AnalyzedIngredient>): List<IngredientRowState> =
        assertNotNull(show(Prototype(analysis = withIngredients(ingredients))).ingredients).rows

    private fun knownName(id: Int, eNumber: String) = withENumber(
        name = UiArg.Nested(UiText.Res(id)),
        eNumber = eNumber,
    )

    private fun show(prototype: Prototype): ProductContentState.ShowProduct =
        assertIs<ProductContentState.ShowProduct>(factory.createFor(prototype).content)

    private fun highlightsOf(analysis: ProductAnalysis): List<Rating?> =
        show(Prototype(analysis = analysis)).macros.values.map { it.highlight }

    private fun scoresOf(product: Product): ScoresState =
        assertNotNull(show(Prototype(analysis = rated(dietRate = DietRate.GOOD, product = product))).scores)

    private companion object {
        const val CARBS = 15.0
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
            nutriScore: NutriScoreGrade? = null,
            novaGroup: NovaGroup? = null,
        ) = Product(
            name = name,
            barcode = "3017620422003",
            brand = brand,
            nutriments = nutriments,
            imageUrl = imageUrl,
            servingAmount = servingAmount,
            category = category,
            nutriScore = nutriScore,
            novaGroup = novaGroup,
        )

        fun rated(
            dietRate: DietRate,
            product: Product = product(),
            mode: DietMode = DietMode.KETO,
            reasons: List<RatingReason> = emptyList(),
        ) = ProductAnalysis.Rated(
            product = product,
            macronutrients = MacronutrientAnalysisResult(
                dietMode = mode,
                rate = dietRate,
                reasons = reasons,
                carbsPer100g = CARBS,
                dailyCarbConsumption = DAILY_CARBS,
            ),
        )

        fun ratedForCarbsAndSugars() = rated(
            dietRate = DietRate.NEUTRAL,
            product = product(nutriments = fullNutriments),
            reasons = listOf(
                RatingReason.HighCarbs(gramsPer100g = 13.0, rate = DietRate.NEUTRAL),
                RatingReason.HighSugars(gramsPer100g = 7.0, rate = DietRate.NEUTRAL),
            ),
        )

        fun highlights(carbs: Rating?, sugars: Rating?): List<Rating?> = listOf(carbs, sugars, null, null, null, null)

        fun ingredient(
            name: String,
            id: String = "en:not-described",
            labelName: String = name,
            eNumber: String? = null,
            vitamin: Vitamin? = null,
            kind: IngredientKind = IngredientKind.PLAIN,
            declaredPercent: Double? = null,
            contains: List<AnalyzedIngredient> = emptyList(),
        ) = AnalyzedIngredient(
            id = id,
            name = IngredientName(name = name, labelName = labelName, eNumber = eNumber, vitamin = vitamin),
            declaredPercent = declaredPercent,
            kind = kind,
            contains = contains,
        )

        val twoContents = listOf(ingredient(name = "water"), ingredient(name = "salt"))

        val sugarSections = persistentListOf(
            what(R.string.product_ingredient_about_sugar_sugar_cane_sugar),
            does(R.string.product_ingredient_about_adds_sweetness),
        )

        fun withIngredients(ingredients: List<AnalyzedIngredient>) = rated(DietRate.GOOD).copy(
            ingredients = IngredientsAnalysis(ingredients = ingredients),
        )

        fun what(text: Int) = IngredientSectionState(
            heading = UiText.Res(R.string.product_ingredient_what),
            text = UiText.Res(text),
        )

        fun does(text: Int) = IngredientSectionState(
            heading = UiText.Res(R.string.product_ingredient_does),
            text = UiText.Res(text),
        )

        fun withENumber(name: UiArg, eNumber: String) = UiText.Res(
            R.string.product_ingredient_name_with_e_number,
            name,
            UiArg.Text(eNumber),
        )

        fun inside(count: Int) = UiText.Plural(
            R.plurals.product_ingredient_inside,
            count,
            UiArg.Numeric.Count(count.toLong()),
        )

        fun spoken(text: UiText, detail: UiText) = UiText.Res(
            R.string.product_ingredient_description,
            UiArg.Nested(text),
            UiArg.Nested(detail),
        )

        fun onTheLabel(name: String) = UiText.Res(R.string.product_ingredient_label_name, UiArg.Text(name))

        fun kcal(value: Double) = UiText.Res(R.string.product_value_kcal, UiArg.Numeric.Kcal(value))

        fun grams(value: Double) = UiText.Res(R.string.product_value_grams, UiArg.Numeric.Grams(value))
    }
}
