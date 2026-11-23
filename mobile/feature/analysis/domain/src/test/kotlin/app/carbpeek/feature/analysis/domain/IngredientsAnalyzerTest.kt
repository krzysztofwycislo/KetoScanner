package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.IngredientName
import app.carbpeek.feature.analysis.api.model.IngredientsAnalysis
import app.carbpeek.feature.analysis.api.model.Vitamin
import app.carbpeek.feature.product.api.model.Ingredient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class IngredientsAnalyzerTest {

    @Test
    fun `when there is no ingredient list then there is no analysis`() {
        assertNull(IngredientsAnalyzer.analyze(null))
        assertNull(IngredientsAnalyzer.analyze(emptyList()))
    }

    @Test
    fun `when only label notes are listed then there is no analysis`() {
        val note = ingredient("en:may-contain-nuts").copy(labelName = "May contain nuts")

        assertNull(IngredientsAnalyzer.analyze(listOf(note)))
    }

    @Test
    fun `when ingredients are listed then label order percent and own kind are kept`() {
        val analysis = analyze(
            ingredient(id = "en:hazelnut", percent = 13.0),
            ingredient("en:sugar"),
            ingredient("en:erythritol"),
            ingredient("en:milk"),
        )

        assertEquals(listOf("hazelnut", "sugar", "erythritol", "milk"), analysis.ingredients.map { it.name.name })
        assertEquals(listOf(13.0, null, null, null), analysis.ingredients.map { it.declaredPercent })
        assertEquals(
            listOf(
                IngredientKind.PLAIN,
                IngredientKind.ADDED_SUGAR,
                IngredientKind.POLYOL,
                IngredientKind.MILK,
            ),
            analysis.ingredients.map { it.kind },
        )
    }

    @Test
    fun `when a compound ingredient contains sugar then it keeps its own kind and lists its contents`() {
        val mustard = ingredient(
            id = "en:mustard",
            sub = listOf(
                ingredient("en:water"),
                ingredient("en:mustard"),
                ingredient("en:vinegar"),
                ingredient("en:sugar"),
            ),
        )

        val analyzed = analyze(mustard).ingredients.single()

        assertEquals(IngredientKind.PLAIN, analyzed.kind)
        assertEquals(listOf("water", "mustard", "vinegar", "sugar"), analyzed.contains.map { it.name.name })
    }

    @Test
    fun `when a compound is listed then its contents keep label order percent and own kind`() {
        val coating = ingredient(
            id = "en:coating",
            percent = 34.0,
            sub = listOf(
                ingredient("en:salt"),
                ingredient(id = "en:sugar", percent = 5.0),
                ingredient("en:wheat-flour"),
                ingredient("en:e500"),
                ingredient("en:e101").copy(labelName = "ryboflawina"),
            ),
        )

        val analyzed = analyze(coating).ingredients.single()

        assertEquals(IngredientKind.PLAIN, analyzed.kind)
        assertEquals(34.0, analyzed.declaredPercent)
        assertEquals(
            listOf("salt", "sugar", "wheat-flour", "e500", "e101"),
            analyzed.contains.map { it.name.name },
        )
        assertEquals(listOf(null, 5.0, null, null, null), analyzed.contains.map { it.declaredPercent })
        assertEquals(
            listOf(
                IngredientKind.PLAIN,
                IngredientKind.ADDED_SUGAR,
                IngredientKind.STARCH,
                IngredientKind.ADDITIVE,
                IngredientKind.PLAIN,
            ),
            analyzed.contains.map { it.kind },
        )
        assertEquals(listOf(null, null, null, null, Vitamin.RIBOFLAVIN), analyzed.contains.map { it.name.vitamin })
    }

    @Test
    fun `when a content has contents of its own then they are not listed`() {
        val mustard = ingredient(
            id = "en:mustard",
            sub = listOf(ingredient("en:water"), ingredient("en:sugar")),
        )
        val sauce = ingredient(id = "en:sauce", sub = listOf(mustard, ingredient("en:salt")))

        val contents = analyze(sauce).ingredients.single().contains

        assertEquals(listOf("mustard", "salt"), contents.map { it.name.name })
        assertEquals(listOf(emptyList(), emptyList()), contents.map { it.contains })
        assertEquals(2, analyze(mustard).ingredients.single().contains.size)
    }

    @Test
    fun `when the parent id is not known then its contents are still listed`() {
        val coating = ingredient(
            id = "fr:chocolat au LAIT avec édulcorant",
            sub = listOf(ingredient("en:e965"), ingredient("en:cocoa-butter")),
        )

        val analyzed = analyze(coating).ingredients.single()

        assertEquals(IngredientKind.PLAIN, analyzed.kind)
        assertEquals(listOf("E965", null), analyzed.contains.map { it.name.eNumber })
    }

    @Test
    fun `when water is nested next to a flour then the contents are listed`() {
        val sourdough = ingredient(
            id = "en:sourdough",
            sub = listOf(ingredient("en:water"), ingredient("en:wheat-flour")),
        )

        assertEquals(
            listOf("water", "wheat-flour"),
            analyze(sourdough).ingredients.single().contains.map { it.name.name },
        )
    }

    @Test
    fun `when salt is nested next to a flour then the contents are listed`() {
        val breadcrumbs = ingredient(
            id = "en:breadcrumbs",
            sub = listOf(ingredient("en:wheat-flour"), ingredient("en:salt")),
        )

        assertEquals(
            listOf("wheat-flour", "salt"),
            analyze(breadcrumbs).ingredients.single().contains.map { it.name.name },
        )
    }

    @Test
    fun `when an ingredient has one nested ingredient then it contains nothing`() {
        val preservative = ingredient(id = "en:preservative", sub = listOf(ingredient("en:e202")))

        assertEquals(emptyList(), analyze(preservative).ingredients.single().contains)
    }

    @Test
    fun `when an oil or fat lists its sources then it contains nothing`() {
        val sources = listOf(ingredient("en:sunflower-oil"), ingredient("en:corn"), ingredient("en:e306"))

        listOf("en:vegetable-oil", "en:vegetable-oils", "en:palm-fat", "en:vegetable-fats").forEach { id ->
            assertEquals(
                emptyList(),
                analyze(ingredient(id = id, sub = sources)).ingredients.single().contains,
                id,
            )
        }
    }

    @Test
    fun `when the nested ingredients are sources only then it contains nothing`() {
        val fibre = ingredient(id = "en:fiber", sub = listOf(ingredient("en:corn"), ingredient("en:wheat")))

        assertEquals(emptyList(), analyze(fibre).ingredients.single().contains)
    }

    @Test
    fun `when an ingredient has an e number id then the name carries the e number and the label name`() {
        val analysis = analyze(
            ingredient("en:e968").copy(name = "E968", labelName = "eritritol"),
            ingredient("en:e500ii"),
            ingredient("en:salt"),
        )

        assertEquals(
            IngredientName(name = "E968", labelName = "eritritol", eNumber = "E968", vitamin = null),
            analysis.ingredients[0].name,
        )
        assertEquals("E500ii", analysis.ingredients[1].name.eNumber)
        assertNull(analysis.ingredients[2].name.eNumber)
    }

    @Test
    fun `when an ingredient is a vitamin then it is plain and its name has no e number`() {
        val analyzed = analyze(ingredient("en:e375").copy(name = "E375", labelName = "niacyna")).ingredients.single()

        assertEquals(IngredientKind.PLAIN, analyzed.kind)
        assertEquals(
            IngredientName(name = "E375", labelName = "niacyna", eNumber = null, vitamin = Vitamin.NIACIN),
            analyzed.name,
        )
    }

    @Test
    fun `when vitamins are nested then they count as contents and are named as vitamins`() {
        listOf(
            ingredient("en:e375").copy(labelName = "niacyna") to Vitamin.NIACIN,
            ingredient("en:e101i").copy(labelName = "ryboflawina") to Vitamin.RIBOFLAVIN,
        ).forEach { (nested, vitamin) ->
            val vitamins = ingredient(id = "en:vitamins", sub = listOf(ingredient("en:vitamin-b6"), nested))

            val analyzed = analyze(vitamins).ingredients.single()

            assertEquals(IngredientKind.PLAIN, analyzed.kind)
            assertEquals(listOf(null, vitamin), analyzed.contains.map { it.name.vitamin })
            assertEquals(listOf(null, null), analyzed.contains.map { it.name.eNumber })
        }
    }

    @Test
    fun `when nested riboflavin prints its e number then it is named as an additive`() {
        val icing = ingredient(
            id = "en:icing",
            sub = listOf(ingredient("en:e101").copy(labelName = "E 101"), ingredient("en:cocoa-butter")),
        )

        val contents = analyze(icing).ingredients.single().contains

        assertEquals(listOf("E101", null), contents.map { it.name.eNumber })
        assertEquals(listOf(null, null), contents.map { it.name.vitamin })
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
        labelName = id.substringAfter(":"),
        declaredPercent = percent,
        subIngredients = sub,
    )
