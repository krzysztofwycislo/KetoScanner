package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.model.IngredientKind
import kotlin.test.Test
import kotlin.test.assertEquals

class IngredientRulesTest {

    @Test
    fun `e numbers are split into polyols sweeteners modified starches and additives`() {
        assertEquals(IngredientKind.POLYOL, ingredientKindOf("en:e965"))
        assertEquals(IngredientKind.SWEETENER, ingredientKindOf("en:e968"))
        assertEquals(IngredientKind.SWEETENER, ingredientKindOf("en:e955"))
        assertEquals(IngredientKind.STARCH, ingredientKindOf("en:e1422"))
        assertEquals(IngredientKind.ADDITIVE, ingredientKindOf("en:e322i"))
        assertEquals(IngredientKind.ADDITIVE, ingredientKindOf("en:e330"))
    }

    @Test
    fun `named polyols win over the syrup rule`() {
        assertEquals(IngredientKind.POLYOL, ingredientKindOf("en:maltitol-syrup"))
        assertEquals(IngredientKind.POLYOL, ingredientKindOf("en:xylitol"))
    }

    @Test
    fun `named no carb sweeteners are sweeteners`() {
        assertEquals(IngredientKind.SWEETENER, ingredientKindOf("en:erythritol"))
        assertEquals(IngredientKind.SWEETENER, ingredientKindOf("en:steviol-glycosides"))
    }

    @Test
    fun `sugars syrups and honey are added sugars`() {
        assertEquals(IngredientKind.ADDED_SUGAR, ingredientKindOf("en:sugar"))
        assertEquals(IngredientKind.ADDED_SUGAR, ingredientKindOf("en:glucose-syrup"))
        assertEquals(IngredientKind.ADDED_SUGAR, ingredientKindOf("en:cane-sugar"))
        assertEquals(IngredientKind.ADDED_SUGAR, ingredientKindOf("en:honey"))
        assertEquals(IngredientKind.ADDED_SUGAR, ingredientKindOf("EN:SUGAR"))
    }

    @Test
    fun `starchy flours grains and starches are starches`() {
        assertEquals(IngredientKind.STARCH, ingredientKindOf("en:wheat-flour"))
        assertEquals(IngredientKind.STARCH, ingredientKindOf("en:potato-starch"))
        assertEquals(IngredientKind.STARCH, ingredientKindOf("en:rice"))
        assertEquals(IngredientKind.STARCH, ingredientKindOf("en:durum-wheat-semolina"))
    }

    @Test
    fun `nut and seed flours are plain`() {
        assertEquals(IngredientKind.PLAIN, ingredientKindOf("en:almond-flour"))
        assertEquals(IngredientKind.PLAIN, ingredientKindOf("en:coconut-flour"))
    }

    @Test
    fun `milk ingredients are lactose`() {
        assertEquals(IngredientKind.LACTOSE, ingredientKindOf("en:skimmed-milk-powder"))
        assertEquals(IngredientKind.LACTOSE, ingredientKindOf("en:whey"))
    }

    @Test
    fun `unknown other language and empty ids are plain`() {
        assertEquals(IngredientKind.PLAIN, ingredientKindOf("pl:cukier"))
        assertEquals(IngredientKind.PLAIN, ingredientKindOf(""))
        assertEquals(IngredientKind.PLAIN, ingredientKindOf("en:palm-oil"))
    }
}
