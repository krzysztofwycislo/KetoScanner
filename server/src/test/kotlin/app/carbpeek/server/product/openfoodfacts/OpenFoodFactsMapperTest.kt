package app.carbpeek.server.product.openfoodfacts

import app.carbpeek.server.product.NutriScore
import app.carbpeek.server.product.Nutrients
import app.carbpeek.server.product.ProductCategory
import app.carbpeek.server.product.noNutrients
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OpenFoodFactsMapperTest {

    private val mustard = ApiIngredient(
        id = "en:mustard",
        text = "mustard",
        isInTaxonomy = 1,
        ingredients = listOf(
            ApiIngredient(id = "en:water", text = "water", isInTaxonomy = 1),
            ApiIngredient(id = "en:salt", text = "salt", isInTaxonomy = 1),
        ),
    )

    private val nutriments = ApiNutriments(
        energyPer100g = 512.0,
        energyPerServing = 206.0,
        fatsPer100g = 30.0,
        fatsPerServing = 15.0,
        saturatedFatPer100g = 14.0,
        saturatedFatPerServing = 7.0,
        carbohydratesPer100g = 44.0,
        carbohydratesPerServing = 22.0,
        sugarsPer100g = 10.0,
        sugarsPerServing = 5.0,
        proteinsPer100g = 20.0,
        proteinsPerServing = 10.0,
        saltPer100g = 3.0,
        saltPerServing = 1.5,
        fibrePer100g = 6.0,
        fibrePerServing = 3.0,
    )

    @Test
    fun `when a product is mapped then its texts and category are kept and the barcode is the asked one`() {
        val apiProduct = ApiProduct(
            productName = "test name",
            frontImage = "test url",
            brand = "test brand",
            servingQuantity = "50",
            categoriesTags = listOf("en:dairies", "en:fermented-milk-products", "en:cheeses"),
            ingredientsText = "milk, salt",
            language = "pl",
        )

        val product = apiProduct.toProduct(barcode = "04963406")

        assertEquals(expected = "04963406", actual = product.barcode)
        assertEquals(expected = "test name", actual = product.name)
        assertEquals(expected = "test url", actual = product.imageUrl)
        assertEquals(expected = "test brand", actual = product.brand)
        assertEquals(expected = "50", actual = product.servingQuantity)
        assertEquals(expected = ProductCategory.CHEESE, actual = product.category)
        assertEquals(expected = "milk, salt", actual = product.ingredientsText)
        assertEquals(expected = "pl", actual = product.labelLanguage)
    }

    @Test
    fun `when nutriments are mapped then per 100 g and per serving go to the matching fields`() {
        val product = ApiProduct(nutriments = nutriments).toProduct(barcode = "04963406")

        assertEquals(
            expected = Nutrients(
                energyKcal = 512.0,
                fat = 30.0,
                saturatedFat = 14.0,
                carbohydrates = 44.0,
                sugars = 10.0,
                fibre = 6.0,
                proteins = 20.0,
                salt = 3.0,
            ),
            actual = product.nutrientsPer100g,
        )
        assertEquals(
            expected = Nutrients(
                energyKcal = 206.0,
                fat = 15.0,
                saturatedFat = 7.0,
                carbohydrates = 22.0,
                sugars = 5.0,
                fibre = 3.0,
                proteins = 10.0,
                salt = 1.5,
            ),
            actual = product.nutrientsPerServing,
        )
    }

    @Test
    fun `when a product has no fields then everything optional is null and the category is other`() {
        val product = ApiProduct().toProduct(barcode = "04963406")

        assertNull(product.name)
        assertNull(product.brand)
        assertNull(product.imageUrl)
        assertNull(product.servingQuantity)
        assertNull(product.labelLanguage)
        assertNull(product.ingredientsText)
        assertNull(product.ingredients)
        assertNull(product.nutriScore)
        assertNull(product.novaGroup)
        assertEquals(expected = ProductCategory.OTHER, actual = product.category)
        assertEquals(expected = noNutrients, actual = product.nutrientsPer100g)
        assertEquals(expected = noNutrients, actual = product.nutrientsPerServing)
    }

    @Test
    fun `when the Nutri-Score is a to e in any case then it is read and anything else is null`() {
        assertEquals(expected = NutriScore.A, actual = nutriScoreOf("A"))
        assertEquals(expected = NutriScore.C, actual = nutriScoreOf("c"))
        assertNull(nutriScoreOf("unknown"))
        assertNull(nutriScoreOf("not-applicable"))
        assertNull(nutriScoreOf(null))
    }

    @Test
    fun `when the NOVA group is 1 to 4 then it is read and anything else is null`() {
        assertEquals(expected = 1, actual = novaGroupOf("1"))
        assertEquals(expected = 4, actual = novaGroupOf("4"))
        assertNull(novaGroupOf("7"))
        assertNull(novaGroupOf("0"))
        assertNull(novaGroupOf("x"))
        assertNull(novaGroupOf(null))
    }

    @Test
    fun `when the ingredient list is empty or textless then it becomes null`() {
        val textless = listOf(ApiIngredient(id = "en:sugar", text = " "))

        assertNull(emptyList<ApiIngredient>().toIngredients(language = "pl", labelText = null))
        assertNull(textless.toIngredients(language = "pl", labelText = null))
    }

    @Test
    fun `when a recognised ingredient is on a foreign label then it gets its English name and keeps the label text`() {
        val chocolate = ApiIngredient(
            id = "en:milk-chocolate",
            text = "czekolada mleczna",
            isInTaxonomy = 1,
            ingredients = listOf(ApiIngredient(id = "en:e322", text = "lecytyny", isInTaxonomy = 1)),
        )

        val ingredient = assertNotNull(
            listOf(chocolate).toIngredients(language = "pl", labelText = "kakao, czekolada mleczna (lecytyny)"),
        ).single()

        assertEquals(expected = "milk chocolate", actual = ingredient.name)
        assertEquals(expected = "czekolada mleczna", actual = ingredient.labelName)
        assertEquals(expected = "E322", actual = ingredient.subIngredients.single().name)
        assertEquals(expected = "lecytyny", actual = ingredient.subIngredients.single().labelName)
    }

    @Test
    fun `when an ingredient is unrecognised or the label is English then it keeps the label text`() {
        val unknown = ApiIngredient(id = "pl:mieszanka-przypraw", text = "mieszanka przypraw", isInTaxonomy = 0)
        val untagged = ApiIngredient(id = "en:spice-mix", text = "przyprawy")
        val english = ApiIngredient(id = "en:e338", text = "phosphoric acid", isInTaxonomy = 1)

        assertEquals(
            expected = listOf("mieszanka przypraw", "przyprawy"),
            actual = listOf(unknown, untagged).toIngredients(language = "pl", labelText = null)?.map { it.name },
        )
        assertEquals(
            expected = "phosphoric acid",
            actual = listOf(english).toIngredients(language = "en", labelText = null)?.single()?.name,
        )
    }

    @Test
    fun `when the label opens a bracket right after the name then nested ingredients are kept`() {
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
            assertEquals(
                expected = listOf("water", "salt"),
                actual = nestedNames(ingredient = mustard, labelText = labelText),
                message = labelText,
            )
        }
    }

    @Test
    fun `when the label does not clearly open a bracket after the name then nested ingredients are dropped`() {
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
            assertEquals(
                expected = emptyList(),
                actual = nestedNames(ingredient = mustard, labelText = labelText),
                message = labelText,
            )
        }
    }

    @Test
    fun `when one nested ingredient is not in the English taxonomy then all nested ingredients are dropped`() {
        val labelText = "mustard (water, salt)"
        val water = ApiIngredient(id = "en:water", text = "water", isInTaxonomy = 1)

        listOf(
            ApiIngredient(id = "en:salt", text = "salt", isInTaxonomy = 0),
            ApiIngredient(id = "en:salt", text = "salt"),
            ApiIngredient(id = "pl:sól", text = "salt", isInTaxonomy = 1),
            ApiIngredient(text = "salt", isInTaxonomy = 1),
        ).forEach { salt ->
            assertEquals(
                expected = emptyList(),
                actual = nestedNames(
                    ingredient = mustard.copy(ingredients = listOf(water, salt)),
                    labelText = labelText,
                ),
                message = "$salt",
            )
        }
    }

    @Test
    fun `when nested ingredients have nested ingredients then the second level is dropped`() {
        val sauce = ApiIngredient(id = "en:sauce", text = "sauce", isInTaxonomy = 1, ingredients = listOf(mustard))

        val ingredient = assertNotNull(
            listOf(sauce).toIngredients(language = "en", labelText = "sauce (mustard (water, salt))"),
        ).single()

        assertEquals(expected = emptyList(), actual = ingredient.subIngredients.single().subIngredients)
    }

    @Test
    fun `when a tag is English then its name has spaces and E-numbers are capitalised`() {
        assertEquals(expected = "soluble corn fiber", actual = englishIngredientNameOf("en:soluble-corn-fiber"))
        assertEquals(expected = "E150d", actual = englishIngredientNameOf("en:e150d"))
        assertNull(englishIngredientNameOf("pl:cukier"))
        assertNull(englishIngredientNameOf("en:"))
    }

    private fun nestedNames(
        ingredient: ApiIngredient,
        labelText: String?,
    ): List<String> =
        assertNotNull(listOf(ingredient).toIngredients(language = "en", labelText = labelText))
            .single()
            .subIngredients
            .map { it.name }
}
