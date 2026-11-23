package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.IngredientKind
import app.carbpeek.feature.analysis.api.model.Vitamin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class IngredientRulesTest {

    @Test
    fun `e numbers are split into polyols sweeteners modified starches and additives`() {
        assertKind(IngredientKind.POLYOL, "en:e965", "en:e968", "en:e964", "en:e420ii")
        assertKind(IngredientKind.SWEETENER, "en:e955", "en:e960a", "en:e969")
        assertKind(IngredientKind.STARCH, "en:e1422")
        assertKind(IngredientKind.ADDITIVE, "en:e322i", "en:e330", "en:e422", "en:e1200", "en:e410")
    }

    @Test
    fun `niacin is a plain vitamin for every label name`() {
        listOf("en:e375", "EN:E375").forEach { id ->
            listOf("niacyna", "Niacin", "E375").forEach { labelName ->
                assertVitamin(expected = Vitamin.NIACIN, id = id, labelName = labelName)
            }
        }
    }

    @Test
    fun `riboflavin is a plain vitamin when the label does not print its e number`() {
        listOf("en:e101", "en:e101i", "en:e101ii", "en:e101a").forEach { id ->
            listOf("vitamine B2", "ryboflawina", "riboflavin (vitamin B2)", "riboflavin 1010").forEach { labelName ->
                assertVitamin(expected = Vitamin.RIBOFLAVIN, id = id, labelName = labelName)
            }
        }
    }

    @Test
    fun `riboflavin is an additive with its e number when the label prints the e number`() {
        listOf("e101", "E 101", "E 101", "E-101(i)", "Farbstoff E101", "riboflavin (e101i)").forEach { labelName ->
            val riboflavin = ingredient("en:e101i").copy(labelName = labelName)

            assertNull(vitaminOf(riboflavin), labelName)
            assertEquals(IngredientKind.ADDITIVE, ingredientKindOf(riboflavin), labelName)
            assertEquals("E101i", eNumberOf(riboflavin), labelName)
        }
    }

    @Test
    fun `riboflavin is an additive when the label prints the e number in brackets after its name`() {
        val riboflavin = ingredient(id = "en:e101", sub = listOf(ingredient("en:e101")))
            .copy(labelName = "riboflavine")

        assertNull(vitaminOf(riboflavin))
        assertEquals(IngredientKind.ADDITIVE, ingredientKindOf(riboflavin))
    }

    @Test
    fun `vitamins that are antioxidants or colours on labels stay additives`() {
        listOf("en:e300", "en:e301", "en:e304", "en:e306", "en:e307", "en:e160a").forEach { id ->
            val additive = ingredient(id).copy(labelName = "vitamin")

            assertNull(vitaminOf(additive), id)
            assertEquals(IngredientKind.ADDITIVE, ingredientKindOf(additive), id)
            assertNotNull(eNumberOf(additive), id)
        }
    }

    @Test
    fun `e numbers that only start like a vitamin are additives`() {
        assertKind(IngredientKind.ADDITIVE, "en:e1010", "en:e1011")
    }

    @Test
    fun `named polyols win over the syrup rule`() {
        assertKind(IngredientKind.POLYOL, "en:maltitol-syrup", "en:xylitol", "en:erythritol")
    }

    @Test
    fun `named sweeteners are sweeteners`() {
        assertKind(IngredientKind.SWEETENER, "en:steviol-glycosides", "en:sweetener")
    }

    @Test
    fun `lecithins locust bean gum and additive classes are additives`() {
        assertKind(
            IngredientKind.ADDITIVE,
            "en:soya-lecithin",
            "en:sunflower-lecithin",
            "en:lecithin",
            "en:carob-seed-flour",
            "en:emulsifier",
            "en:preservative",
            "en:acidity-regulator",
            "en:flour-treatment-agent",
        )
    }

    @Test
    fun `sugars syrups and honey are added sugars`() {
        assertKind(
            IngredientKind.ADDED_SUGAR,
            "en:sugar",
            "en:glucose-syrup",
            "en:cane-sugar",
            "en:honey",
            "en:sugar-from-beets",
            "en:corn-syrup-solids",
            "en:malt-extract",
            "EN:SUGAR",
        )
    }

    @Test
    fun `names that only mention sugar syrup or honey are not added sugars`() {
        assertKind(
            IngredientKind.PLAIN,
            "en:oligofructose-syrup",
            "en:rakia-sugar",
            "en:honey-flavouring",
            "en:honeydew-melon",
            "en:honey-mushroom",
            "en:brewed-without-sugar",
            "en:fruit-with-sugar",
            "en:no-added-sugar",
            "en:caramel",
        )
    }

    @Test
    fun `starchy flours grains and starches are starches`() {
        assertKind(
            IngredientKind.STARCH,
            "en:wheat-flour",
            "en:potato-starch",
            "en:rice",
            "en:durum-wheat-semolina",
            "en:maltodextrin",
        )
    }

    @Test
    fun `nut seed and konjac flours are plain`() {
        assertKind(
            IngredientKind.PLAIN,
            "en:almond-flour",
            "en:coconut-flour",
            "en:konjac-flour",
            "en:acerola-powder-on-manioc-starch",
        )
    }

    @Test
    fun `dairy is a milk ingredient`() {
        assertKind(
            IngredientKind.MILK,
            "en:skimmed-milk-powder",
            "en:whey",
            "en:sweetened-condensed-milk",
            "en:cream",
            "en:butter",
            "en:cheese",
            "en:grana-padano",
            "en:milk",
        )
    }

    @Test
    fun `foods that only sound like dairy are plain`() {
        assertKind(IngredientKind.PLAIN, "en:peanut-butter", "en:cocoa-butter", "en:coconut-milk")
    }

    @Test
    fun `carb heavy foods without a rule are plain`() {
        assertKind(IngredientKind.PLAIN, "en:dates", "en:orange-juice", "en:long-grain-basmati-rice")
    }

    @Test
    fun `unknown other language and empty ids are plain`() {
        assertKind(
            IngredientKind.PLAIN,
            "pl:cukier",
            "",
            "en:palm-oil",
            "fr:agave syrup",
            "en:agave syrup",
            "fr:rice-flour",
        )
    }

    private fun assertKind(expected: IngredientKind, vararg ids: String) {
        ids.forEach { id -> assertEquals(expected, ingredientKindOf(ingredient(id)), id) }
    }

    private fun assertVitamin(expected: Vitamin, id: String, labelName: String) {
        val vitamin = ingredient(id).copy(labelName = labelName)

        assertEquals(expected, vitaminOf(vitamin), "$id $labelName")
        assertEquals(IngredientKind.PLAIN, ingredientKindOf(vitamin), "$id $labelName")
        assertNull(eNumberOf(vitamin), "$id $labelName")
    }
}
