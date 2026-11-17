package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.IngredientCounts
import app.carbpeek.feature.analysis.api.model.IngredientFinding
import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.IngredientsAnalysis
import app.carbpeek.feature.product.api.model.Ingredient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IngredientsAnalyzerTest {

    @Test
    fun `when there is no ingredient list then there is no analysis`() {
        assertNull(IngredientsAnalyzer.analyze(null))
        assertNull(IngredientsAnalyzer.analyze(emptyList()))
    }

    @Test
    fun `when sugar is listed first then it is a major carb source and named in the finding`() {
        val analysis = analyze(ingredient("en:sugar"), ingredient("en:palm-oil"), ingredient("en:hazelnut"))

        val finding = assertIs<IngredientFinding.CarbSource>(analysis.finding)
        assertEquals("sugar", finding.ingredient.name)
        assertEquals(1, finding.ingredient.position)
        assertTrue(finding.ingredient.isMajor)
    }

    @Test
    fun `when sugar is fourth without a declared percent then it is a minor carb source`() {
        val analysis = analyze(
            ingredient("en:hazelnut"),
            ingredient("en:palm-oil"),
            ingredient("en:cocoa"),
            ingredient("en:sugar"),
        )

        val finding = assertIs<IngredientFinding.CarbSource>(analysis.finding)
        assertEquals("sugar", finding.ingredient.name)
        assertFalse(finding.ingredient.isMajor)
    }

    @Test
    fun `when a declared percent is below the threshold then a top ingredient is minor`() {
        val analysis = analyze(ingredient("en:hazelnut"), ingredient(id = "en:sugar", percent = 5.0))

        assertFalse(analysis.ingredients[1].isMajor)
    }

    @Test
    fun `when a declared percent reaches the threshold then a late ingredient is major`() {
        val analysis = analyze(
            ingredient("en:hazelnut"),
            ingredient("en:palm-oil"),
            ingredient("en:cocoa"),
            ingredient("en:salt"),
            ingredient("en:vanilla"),
            ingredient(id = "en:sugar", percent = 12.0),
        )

        assertTrue(analysis.ingredients[5].isMajor)
    }

    @Test
    fun `when only a no carb sweetener is added then the sweetener is named`() {
        val analysis = analyze(ingredient("en:water"), ingredient("en:erythritol"))

        val finding = assertIs<IngredientFinding.NoCarbSweetener>(analysis.finding)
        assertEquals("erythritol", finding.ingredient.name)
    }

    @Test
    fun `when only milk and cream are listed then nothing is added`() {
        val analysis = analyze(ingredient("en:milk"), ingredient("en:cream"))

        assertEquals(IngredientFinding.NothingAdded, analysis.finding)
        assertEquals(IngredientKind.LACTOSE, analysis.ingredients[0].kind)
    }

    @Test
    fun `when a compound ingredient contains sugar then it is an added sugar found in that sugar`() {
        val chocolate = ingredient(
            id = "en:milk-chocolate",
            sub = listOf(
                ingredient("en:cocoa-butter"),
                ingredient(id = "en:filling", sub = listOf(ingredient("en:sugar"))),
                ingredient("en:e322"),
            ),
        )

        val analyzed = analyze(chocolate).ingredients.single()

        assertEquals(IngredientKind.ADDED_SUGAR, analyzed.kind)
        assertEquals("sugar", analyzed.foundIn)
        assertEquals("milk-chocolate", analyzed.name)
    }

    @Test
    fun `when weaker kinds are nested before sugar then the sugar still decides the kind`() {
        val coating = ingredient(
            id = "en:coating",
            sub = listOf(
                ingredient("en:e322"),
                ingredient("en:milk"),
                ingredient("en:sugar"),
            ),
        )

        val analyzed = analyze(coating).ingredients.single()

        assertEquals(IngredientKind.ADDED_SUGAR, analyzed.kind)
        assertEquals("sugar", analyzed.foundIn)
    }

    @Test
    fun `when an ingredient has its own kind then nested ingredients are ignored`() {
        val syrup = ingredient(id = "en:glucose-syrup", sub = listOf(ingredient("en:e955")))

        val analyzed = analyze(syrup).ingredients.single()

        assertEquals(IngredientKind.ADDED_SUGAR, analyzed.kind)
        assertNull(analyzed.foundIn)
    }

    @Test
    fun `the finding is the first major carb source even when a minor one is listed earlier`() {
        val analysis = analyze(
            ingredient("en:pork"),
            ingredient(id = "en:potato-starch", percent = 2.0),
            ingredient("en:palm-oil"),
            ingredient(id = "en:glucose-syrup", percent = 15.0),
            ingredient(id = "en:sugar", percent = 11.0),
        )

        val finding = assertIs<IngredientFinding.CarbSource>(analysis.finding)
        assertEquals("glucose-syrup", finding.ingredient.name)
        assertEquals(4, finding.ingredient.position)
    }

    @Test
    fun `a carb source wins over a no carb sweetener listed before it`() {
        val analysis = analyze(
            ingredient("en:water"),
            ingredient("en:e955"),
            ingredient(id = "en:maltitol", percent = 1.0),
        )

        val finding = assertIs<IngredientFinding.CarbSource>(analysis.finding)
        assertEquals("maltitol", finding.ingredient.name)
    }

    @Test
    fun `counts group added sugars sweeteners starches and additives`() {
        val analysis = analyze(
            ingredient("en:sugar"),
            ingredient("en:glucose-syrup"),
            ingredient("en:maltitol"),
            ingredient("en:e955"),
            ingredient("en:wheat-flour"),
            ingredient("en:e322"),
            ingredient("en:e330"),
            ingredient("en:e471"),
            ingredient("en:milk"),
        )

        assertEquals(
            IngredientCounts(
                addedSugars = 2,
                sweeteners = 2,
                starches = 1,
                additives = 3,
            ),
            analysis.counts,
        )
    }

    private fun analyze(vararg ingredients: Ingredient): IngredientsAnalysis =
        assertNotNull(IngredientsAnalyzer.analyze(ingredients.toList()))
}

internal fun ingredient(
    id: String,
    percent: Double? = null,
    sub: List<Ingredient> = emptyList(),
): Ingredient =
    Ingredient(
        id = id,
        name = id.substringAfter(":"),
        declaredPercent = percent,
        subIngredients = sub,
    )
