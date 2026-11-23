package app.carbpeek.openfoodapi

import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.product.api.model.NovaGroup
import app.carbpeek.feature.product.api.model.NutriScoreGrade
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.openfoodapi.data.ApiIngredient
import app.carbpeek.openfoodapi.data.ApiProduct
import app.carbpeek.openfoodapi.data.GetProductResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class DataConvertersTest {

    @Test
    fun `convert success GetProductResponse with product into product test`() {
        val response = getProductResponseWithProduct()
        val apiProduct = response.apiProduct!!

        val product = parseGetProductResponse(response)!!

        assertEquals(apiProduct.productName, product.name)
        assertEquals(response.barcode, product.barcode)
        assertEquals(ProductCategory.CHEESE, product.category)
    }

    @Test
    fun `convert GetProductResponse without product into null test`() {
        val response = getProductResponseWithoutProduct()

        assertNull(parseGetProductResponse(response))
    }

    @Test
    fun `parse nutriments maps per 100g and per serving values to matching fields`() {
        val nutriments = parseProductNutrients(testApiNutriments())

        assertEquals(44.0, nutriments.carbohydratesPer100g)
        assertEquals(22.0, nutriments.carbohydratesPerServing)
        assertEquals(512.0, nutriments.energyPer100g)
        assertEquals(206.0, nutriments.energyPerServing)
        assertEquals(30.0, nutriments.fatsPer100g)
        assertEquals(15.0, nutriments.fatsPerServing)
        assertEquals(20.0, nutriments.proteinsPer100g)
        assertEquals(10.0, nutriments.proteinsPerServing)
        assertEquals(3.0, nutriments.saltPer100g)
        assertEquals(1.5, nutriments.saltPerServing)
        assertEquals(10.0, nutriments.sugarsPer100g)
        assertEquals(5.0, nutriments.sugarsPerServing)
        assertEquals(6.0, nutriments.fibrePer100g)
        assertEquals(3.0, nutriments.fibrePerServing)
    }

    @Test
    fun `convert product without name and nutriments into product with empty name and null nutriments`() {
        val response = GetProductResponse(
            1,
            "barcode",
            ApiProduct(
                nutriments = null,
                productName = null,
                frontImage = null,
                brand = null,
                servingQuantity = null
            )
        )

        val product = parseGetProductResponse(response)

        assertNotNull(product)
        assertTrue(product.name.isEmpty())
        assertEquals(ProductNutriments(null, null, null, null, null, null, null, null, null, null), product.nutriments)
        assertEquals(ProductCategory.OTHER, product.category)
    }

    @Test
    fun `nutri score grades a to e are read in any case and anything else is null`() {
        assertEquals(NutriScoreGrade.A, nutriScoreGradeOf("A"))
        assertEquals(NutriScoreGrade.C, nutriScoreGradeOf("c"))
        assertNull(nutriScoreGradeOf("unknown"))
        assertNull(nutriScoreGradeOf("not-applicable"))
        assertNull(nutriScoreGradeOf(null))
    }

    @Test
    fun `nova groups 1 to 4 are read and anything else is null`() {
        assertEquals(NovaGroup.ULTRA_PROCESSED, novaGroupOf("4"))
        assertNull(novaGroupOf("7"))
        assertNull(novaGroupOf("x"))
        assertNull(novaGroupOf(null))
    }

    @Test
    fun `an empty or textless ingredient list becomes null`() {
        assertNull(emptyList<ApiIngredient>().toIngredients(language = "pl", labelText = null))
        assertNull(listOf(ApiIngredient(id = "en:sugar", text = " ")).toIngredients(language = "pl", labelText = null))
    }

    @Test
    fun `a recognised ingredient on a foreign label gets its English name and keeps the label text`() {
        val chocolate = ApiIngredient(
            id = "en:milk-chocolate",
            text = "czekolada mleczna",
            isInTaxonomy = 1,
            ingredients = listOf(ApiIngredient(id = "en:e322", text = "lecytyny", isInTaxonomy = 1)),
        )

        val ingredient = assertNotNull(
            listOf(chocolate).toIngredients(language = "pl", labelText = "kakao, czekolada mleczna (lecytyny)"),
        ).single()

        assertEquals("milk chocolate", ingredient.name)
        assertEquals("czekolada mleczna", ingredient.labelName)
        assertEquals("E322", ingredient.subIngredients.single().name)
        assertEquals("lecytyny", ingredient.subIngredients.single().labelName)
    }

    @Test
    fun `an unrecognised ingredient or an English label keeps the label text`() {
        val unknown = ApiIngredient(id = "pl:mieszanka-przypraw", text = "mieszanka przypraw", isInTaxonomy = 0)
        val untagged = ApiIngredient(id = "en:spice-mix", text = "przyprawy")
        val english = ApiIngredient(id = "en:e338", text = "phosphoric acid", isInTaxonomy = 1)

        assertEquals(
            listOf("mieszanka przypraw", "przyprawy"),
            listOf(unknown, untagged).toIngredients(language = "pl", labelText = null)?.map { it.name },
        )
        assertEquals(
            "phosphoric acid",
            listOf(english).toIngredients(language = "en", labelText = null)?.single()?.name,
        )
    }

    @Test
    fun `nested ingredients are kept when the label opens a bracket right after the name`() {
        listOf(
            "canola oil, egg yolk 6,0%, vinegar, mustard (water, salt), sugar",
            "mustard (water, salt)",
            "27% mustard (water, salt), sugar",
            "27,5 % mustard (water, salt), sugar",
            "sugar, (27 %) mustard (water, salt)",
            "sugar, mustard 27% (water, salt)",
            "sugar, mustard (27 %) [water, salt]",
            "sugar, _mustard_* {water, salt}",
            "filling: mustard (water, salt)",
            "sugar, mustard (water, mustard, salt)",
        ).forEach { labelText ->
            assertEquals(listOf("water", "salt"), nestedNames(mustard, labelText), labelText)
        }
    }

    @Test
    fun `nested ingredients are dropped when the label does not clearly open them after the name`() {
        listOf(
            null,
            "",
            "sugar, cream and mustard (water, salt)",
            "crème acidulée et mustard (water, salt)",
            "crème et 27,5% mustard (water, salt)",
            "sugar, mustard: water, salt",
            "sugar, mustard (27%): water, salt",
            "sugar, hot mustard (water, salt)",
            "mustard (water, salt), cream and mustard (pepper)",
        ).forEach { labelText ->
            assertEquals(emptyList(), nestedNames(mustard, labelText), labelText)
        }
    }

    @Test
    fun `nested ingredients are dropped when one of them is not in the English taxonomy`() {
        val labelText = "mustard (water, salt)"
        val water = ApiIngredient(id = "en:water", text = "water", isInTaxonomy = 1)

        listOf(
            ApiIngredient(id = "en:salt", text = "salt", isInTaxonomy = 0),
            ApiIngredient(id = "en:salt", text = "salt"),
            ApiIngredient(id = "pl:sól", text = "salt", isInTaxonomy = 1),
            ApiIngredient(text = "salt", isInTaxonomy = 1),
        ).forEach { salt ->
            assertEquals(emptyList(), nestedNames(mustard.copy(ingredients = listOf(water, salt)), labelText), "$salt")
        }
    }

    @Test
    fun `nested ingredients never have nested ingredients of their own`() {
        val sauce = ApiIngredient(id = "en:sauce", text = "sauce", isInTaxonomy = 1, ingredients = listOf(mustard))

        val ingredient = assertNotNull(
            listOf(sauce).toIngredients(language = "en", labelText = "sauce (mustard (water, salt))"),
        ).single()

        assertEquals(emptyList(), ingredient.subIngredients.single().subIngredients)
    }

    @Test
    fun `English names come from the tag with spaces and E-numbers capitalised`() {
        assertEquals("soluble corn fiber", englishIngredientNameOf("en:soluble-corn-fiber"))
        assertEquals("E150d", englishIngredientNameOf("en:e150d"))
        assertNull(englishIngredientNameOf("pl:cukier"))
        assertNull(englishIngredientNameOf("en:"))
    }

    @Test
    fun `api product without ingredients and scores converts to nulls`() {
        val product = assertNotNull(parseGetProductResponse(getProductResponseWithProduct()))

        assertNull(product.ingredients)
        assertNull(product.nutriScore)
        assertNull(product.novaGroup)
    }

    private fun nestedNames(ingredient: ApiIngredient, labelText: String?): List<String> =
        assertNotNull(listOf(ingredient).toIngredients(language = "en", labelText = labelText))
            .single()
            .subIngredients
            .map { it.name }

    private companion object {
        val mustard = ApiIngredient(
            id = "en:mustard",
            text = "mustard",
            isInTaxonomy = 1,
            ingredients = listOf(
                ApiIngredient(id = "en:water", text = "water", isInTaxonomy = 1),
                ApiIngredient(id = "en:salt", text = "salt", isInTaxonomy = 1),
            ),
        )
    }

}
