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
import app.carbpeek.feature.product.ui.state.IngredientChipState
import app.carbpeek.feature.product.ui.state.IngredientLegendState
import app.carbpeek.feature.product.ui.state.IngredientSectionState
import app.carbpeek.feature.product.ui.state.IngredientSheetState
import app.carbpeek.feature.product.ui.state.IngredientTone
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
            listOf(IngredientTone.ADDED_SUGAR),
            content.ingredients?.chips?.map { it.tone },
        )
    }

    @Test
    fun `when a name is shown then it is upper cased in the chip and the sheet title`() {
        val stevia = ingredient(name = "stevia", kind = IngredientKind.SWEETENER)
        val content = show(
            Prototype(
                analysis = withIngredients(listOf(stevia)),
                openIngredientIndex = 0,
            ),
        )

        assertEquals(UiText.Raw("Stevia"), content.ingredients?.chips?.single()?.label)
        assertEquals(UiText.Raw("Stevia"), content.ingredientSheet?.title)
    }

    @Test
    fun `every ingredient kind maps to its chip tone in label order`() {
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
            ingredient(name = kind.name.lowercase(), kind = kind)
        }

        val chips = assertNotNull(
            show(
                Prototype(
                    analysis = withIngredients(ingredients),
                ),
            ).ingredients,
        ).chips

        assertEquals(tones.values.toList(), chips.map { it.tone })
        assertEquals(
            ingredients.map { UiText.Raw(it.name.name.replaceFirstChar { char -> char.uppercase() }) },
            chips.map { it.label },
        )
    }

    @Test
    fun `when an ingredient declares a percent then the chip label and description include it`() {
        val sugar = ingredient(name = "sugar", kind = IngredientKind.ADDED_SUGAR, declaredPercent = 8.7)
        val salt = ingredient(name = "salt")

        val chips = assertNotNull(
            show(
                Prototype(
                    analysis = withIngredients(listOf(sugar, salt)),
                ),
            ).ingredients,
        ).chips

        val sugarLabel = UiText.Res(
            R.string.product_ingredient_with_percent,
            UiArg.Nested(UiText.Raw("Sugar")),
            UiArg.Numeric.LabelPercent(0.087),
        )
        assertEquals(
            IngredientChipState(
                label = sugarLabel,
                count = null,
                description = UiText.Res(
                    R.string.product_ingredient_description,
                    UiArg.Nested(sugarLabel),
                    UiArg.Nested(UiText.Res(R.string.product_ingredient_tag_added_sugar)),
                ),
                tone = IngredientTone.ADDED_SUGAR,
            ),
            chips[0],
        )
        assertEquals(
            IngredientChipState(
                label = UiText.Raw("Salt"),
                count = null,
                description = UiText.Raw("Salt"),
                tone = IngredientTone.PLAIN,
            ),
            chips[1],
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
        val analysis = withIngredients(listOf(ingredient(name = "milk")))

        assertNull(show(Prototype(analysis = analysis)).ingredientSheet)
        assertNull(show(Prototype(analysis = analysis, openIngredientIndex = 1)).ingredientSheet)
        assertNull(show(Prototype(analysis = analysis, openIngredientIndex = -1)).ingredientSheet)
    }

    @Test
    fun `the legend explains the five types in a fixed order`() {
        val analysis = withIngredients(listOf(ingredient(name = "milk")))
        val sheet = show(Prototype(analysis = analysis, isIngredientsInfoOpen = true)).ingredientsInfoSheet

        assertEquals(
            listOf(
                IngredientTone.ADDED_SUGAR to R.string.product_ingredient_tag_added_sugar,
                IngredientTone.SWEETENER to R.string.product_ingredient_tag_sweetener,
                IngredientTone.STARCH to R.string.product_ingredient_tag_starch,
                IngredientTone.MILK to R.string.product_ingredient_tag_milk,
                IngredientTone.ADDITIVE to R.string.product_ingredient_tag_additive,
            ).map { (tone, label) -> IngredientLegendState(tone = tone, label = UiText.Res(label)) },
            sheet?.legend?.toList(),
        )
    }

    @Test
    fun `when a typed ingredient is open then the sheet says what it is`() {
        val sugar = ingredient(
            name = "sugar",
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
                sections = persistentListOf(what(R.string.product_ingredient_what_added_sugar)),
                noDescription = null,
            ),
            sheet(sugar),
        )
    }

    @Test
    fun `every type shows only texts that are true for the whole type`() {
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
            assertEquals(expected, sheet(ingredient(name = "name", kind = kind)).sections.toList(), "$kind")
        }
    }

    @Test
    fun `polyols and sweeteners share one tag and regular ingredients have none`() {
        val sweetener = UiText.Res(R.string.product_ingredient_tag_sweetener)

        assertEquals(sweetener, sheet(ingredient(name = "maltitol", kind = IngredientKind.POLYOL)).tag)
        assertEquals(sweetener, sheet(ingredient(name = "stevia", kind = IngredientKind.SWEETENER)).tag)
        assertNull(sheet(ingredient(name = "salt")).tag)
    }

    @Test
    fun `when a regular ingredient is open then the sheet says that there is no description`() {
        assertEquals(
            IngredientSheetState(
                back = null,
                tag = null,
                tone = IngredientTone.PLAIN,
                title = UiText.Raw("Salt"),
                labelName = null,
                contents = persistentListOf(),
                sections = persistentListOf(),
                noDescription = UiText.Res(R.string.product_ingredient_no_description),
            ),
            sheet(ingredient(name = "salt")),
        )
    }

    @Test
    fun `when a compound is open then its contents are the chips of the list in order without a count`() {
        val contents = listOf(
            ingredient(name = "wheat flour", kind = IngredientKind.STARCH, declaredPercent = 12.5),
            ingredient(name = "water"),
            ingredient(name = "E965", labelName = "maltitol", eNumber = "E965", kind = IngredientKind.POLYOL),
            ingredient(name = "E322", labelName = "lecytyny", eNumber = "E322", kind = IngredientKind.ADDITIVE),
            ingredient(name = "E375", labelName = "niacyna", vitamin = Vitamin.NIACIN),
        )

        val chips = sheet(ingredient(name = "coating", contains = contents)).contents.toList()

        assertEquals(cardChips(contents), chips)
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
            chips.map { it.label },
        )
        assertTrue(chips.all { it.count == null })
    }

    @Test
    fun `when an ingredient has contents then its chip shows and speaks the count after the type`() {
        val contents = listOf(ingredient(name = "wheat flour"), ingredient(name = "salt"))

        val (typed, untyped, single) = cardChips(
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
    fun `when a compound holds added sugar then its chip and its sheet keep its own type`() {
        val sugar = ingredient(name = "sugar", kind = IngredientKind.ADDED_SUGAR)
        val mustard = ingredient(name = "mustard", contains = listOf(ingredient(name = "water"), sugar))
        val breadcrumbs = ingredient(
            name = "breadcrumbs",
            kind = IngredientKind.STARCH,
            contains = listOf(ingredient(name = "salt"), sugar),
        )

        assertEquals(
            listOf(IngredientTone.PLAIN, IngredientTone.STARCH),
            cardChips(listOf(mustard, breadcrumbs)).map { it.tone },
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

        assertEquals(cardChips(contents), sheet.contents.toList())
        assertEquals(listOf(what(R.string.product_ingredient_what_starch)), sheet.sections.toList())
        assertNull(sheet.noDescription)
    }

    @Test
    fun `when a regular compound is open then its contents are shown and no missing description`() {
        val mustard = ingredient(
            name = "mustard",
            contains = listOf(ingredient(name = "water"), ingredient(name = "salt")),
        )

        val sheet = sheet(mustard)

        assertEquals(2, sheet.contents.size)
        assertNull(sheet.noDescription)
    }

    @Test
    fun `when a typed content is open then its own details and a way back to the compound are shown`() {
        val coating = ingredient(
            name = "coating",
            contains = listOf(
                ingredient(name = "water"),
                ingredient(name = "sugar", labelName = "sucre", kind = IngredientKind.ADDED_SUGAR),
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
                sections = persistentListOf(what(R.string.product_ingredient_what_added_sugar)),
                noDescription = null,
            ),
            sheet(ingredient = coating, contentIndex = 1),
        )
    }

    @Test
    fun `when a regular content is open then the sheet says that there is no description`() {
        val coating = ingredient(
            name = "coating",
            contains = listOf(ingredient(name = "water"), ingredient(name = "salt")),
        )

        val sheet = sheet(ingredient = coating, contentIndex = 0)

        assertNotNull(sheet.back)
        assertEquals(UiText.Raw("Water"), sheet.title)
        assertNull(sheet.tag)
        assertEquals(UiText.Res(R.string.product_ingredient_no_description), sheet.noDescription)
        assertTrue(sheet.contents.isEmpty())
    }

    @Test
    fun `when the open content does not exist then the ingredient is shown without a way back`() {
        val coating = ingredient(
            name = "coating",
            contains = listOf(ingredient(name = "water"), ingredient(name = "salt")),
        )
        val pepper = ingredient(name = "pepper")

        assertEquals(sheet(coating), sheet(ingredient = coating, contentIndex = 2))
        assertEquals(sheet(coating), sheet(ingredient = coating, contentIndex = -1))
        assertEquals(sheet(pepper), sheet(ingredient = pepper, contentIndex = 0))
        assertNull(sheet(coating).back)
        assertNull(sheet(pepper).back)
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
        val erythritol = ingredient(name = "E968", labelName = "eritritol", eNumber = "E968")
        val stevia = ingredient(name = "E960a", labelName = "E 960a", eNumber = "E960a")
        val english = ingredient(name = "xylitol", eNumber = "E967")

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
                sheet(ingredient(name = eNumber, eNumber = eNumber)).title,
                eNumber,
            )
        }
    }

    @Test
    fun `another additive shows its label name with the e number and no label line`() {
        val lecithins = ingredient(name = "E322", labelName = "lecytyny", eNumber = "E322")

        assertEquals(withENumber(name = UiArg.Text("Lecytyny"), eNumber = "E322"), sheet(lecithins).title)
        assertNull(sheet(lecithins).labelName)
    }

    @Test
    fun `when the label prints only a code then the e number is shown`() {
        listOf("E 471", "e471", "E-471", "E471").forEach { labelName ->
            val additive = ingredient(name = "E471", labelName = labelName, eNumber = "E471")

            assertEquals(UiText.Raw("E471"), sheet(additive).title, labelName)
            assertNull(sheet(additive).labelName, labelName)
        }
        assertEquals(
            UiText.Raw("E500ii"),
            sheet(ingredient(name = "E500ii", labelName = "E500(ii)", eNumber = "E500ii")).title,
        )
    }

    @Test
    fun `when the label name already holds the code then the code is not repeated`() {
        assertEquals(
            UiText.Raw("Emulgator E 471"),
            sheet(ingredient(name = "E471", labelName = "emulgator E 471", eNumber = "E471")).title,
        )
        assertEquals(
            UiText.Raw("Sodium carbonates e-500(ii)"),
            sheet(ingredient(name = "E500ii", labelName = "sodium carbonates e-500(ii)", eNumber = "E500ii")).title,
        )
    }

    @Test
    fun `the label line is hidden when the label name differs only in case`() {
        assertNull(sheet(ingredient(name = "sugar", labelName = "SUGAR")).labelName)
    }

    @Test
    fun `a vitamin sheet shows its English name without an e number the label name and no description`() {
        val niacin = ingredient(name = "E375", labelName = "niacyna", vitamin = Vitamin.NIACIN)

        assertEquals(
            IngredientSheetState(
                back = null,
                tag = null,
                tone = IngredientTone.PLAIN,
                title = UiText.Res(R.string.product_ingredient_vitamin_b3),
                labelName = onTheLabel("niacyna"),
                contents = persistentListOf(),
                sections = persistentListOf(),
                noDescription = UiText.Res(R.string.product_ingredient_no_description),
            ),
            sheet(niacin),
        )
    }

    @Test
    fun `a vitamin on an English or coded label still shows the label name`() {
        val english = sheet(ingredient(name = "riboflavin", vitamin = Vitamin.RIBOFLAVIN))
        val coded = sheet(ingredient(name = "E375", vitamin = Vitamin.NIACIN))

        assertEquals(UiText.Res(R.string.product_ingredient_vitamin_b2), english.title)
        assertEquals(onTheLabel("riboflavin"), english.labelName)
        assertEquals(UiText.Res(R.string.product_ingredient_vitamin_b3), coded.title)
        assertEquals(onTheLabel("E375"), coded.labelName)
    }

    @Test
    fun `a vitamin chip and its description are the vitamin name without a type`() {
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

        val chips = assertNotNull(show(Prototype(analysis = analysis)).ingredients).chips

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
                IngredientChipState(
                    label = label,
                    count = null,
                    description = label,
                    tone = IngredientTone.PLAIN,
                )
            },
            chips.toList(),
        )
    }

    private fun sheet(ingredient: AnalyzedIngredient, contentIndex: Int? = null): IngredientSheetState = assertNotNull(
        show(
            Prototype(
                analysis = withIngredients(listOf(ingredient)),
                openIngredientIndex = 0,
                openIngredientContentIndex = contentIndex,
            ),
        ).ingredientSheet,
    )

    private fun cardChips(ingredients: List<AnalyzedIngredient>): List<IngredientChipState> =
        assertNotNull(show(Prototype(analysis = withIngredients(ingredients))).ingredients).chips

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
            labelName: String = name,
            eNumber: String? = null,
            vitamin: Vitamin? = null,
            kind: IngredientKind = IngredientKind.PLAIN,
            declaredPercent: Double? = null,
            contains: List<AnalyzedIngredient> = emptyList(),
        ) = AnalyzedIngredient(
            name = IngredientName(name = name, labelName = labelName, eNumber = eNumber, vitamin = vitamin),
            declaredPercent = declaredPercent,
            kind = kind,
            contains = contains,
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
