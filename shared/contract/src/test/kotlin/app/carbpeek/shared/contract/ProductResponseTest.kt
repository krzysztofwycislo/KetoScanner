package app.carbpeek.shared.contract

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductResponseTest {

    private val full = ProductResponse(
        barcode = "3017620422003",
        name = "Nutella",
        brand = "Ferrero",
        imageUrl = "https://images.openfoodfacts.org/images/products/301/762/042/2003/front_en.jpg",
        servingQuantity = "15",
        category = ProductCategoryCode.SPREADS,
        labelLanguage = "fr",
        ingredientsText = "Sucre, noisettes 13%, chocolat au lait (sucre, beurre de cacao)",
        ingredients = listOf(
            ProductIngredient(
                id = "en:hazelnut",
                name = "hazelnut",
                labelName = "noisettes",
                declaredPercent = 13.0,
            ),
            ProductIngredient(
                id = "en:milk-chocolate",
                name = "milk chocolate",
                labelName = "chocolat au lait",
                subIngredients = listOf(
                    ProductIngredient(
                        id = "en:sugar",
                        name = "sugar",
                        labelName = "sucre",
                    ),
                ),
            ),
        ),
        nutriScore = ProductNutriScore.E,
        novaGroup = 4,
        nutrients = ProductNutrients(
            per100g = NutrientAmounts(
                energyKcal = 539.0,
                fat = 30.9,
                saturatedFat = 10.6,
                carbohydrates = 57.5,
                sugars = 56.3,
                fibre = 3.5,
                proteins = 6.3,
                salt = 0.107,
            ),
            perServing = NutrientAmounts(
                energyKcal = 80.8,
                fat = 4.64,
                saturatedFat = 1.59,
                carbohydrates = 8.62,
                sugars = 8.44,
                fibre = 0.525,
                proteins = 0.945,
                salt = 0.016,
            ),
        ),
    )

    private val minimal = ProductResponse(
        barcode = "04963406",
        category = ProductCategoryCode.OTHER,
    )

    @Test
    fun `when a full product is encoded then it matches the golden file`() {
        assertEquals(expected = parseJson(readGolden("product.json")), actual = parseJson(encode(full)))
    }

    @Test
    fun `when the full golden file is decoded then it gives the sample`() {
        assertEquals(expected = full, actual = decode(readGolden("product.json")))
    }

    @Test
    fun `when a product without optional fields is encoded then it matches the golden file`() {
        assertEquals(expected = parseJson(readGolden("product-minimal.json")), actual = parseJson(encode(minimal)))
    }

    @Test
    fun `when the minimal golden file is decoded then it gives the sample`() {
        assertEquals(expected = minimal, actual = decode(readGolden("product-minimal.json")))
    }

    @Test
    fun `when the category is unknown then it decodes to UNKNOWN`() {
        val decoded = decode("""{"barcode":"04963406","category":"ICE_CREAM","nutrients":{}}""")

        assertEquals(expected = ProductCategoryCode.UNKNOWN, actual = decoded.category)
    }

    @Test
    fun `when the Nutri-Score is unknown then it decodes to null`() {
        val decoded = decode("""{"barcode":"04963406","category":"OTHER","nutriScore":"F","nutrients":{}}""")

        assertNull(decoded.nutriScore)
    }

    @Test
    fun `when an unknown field is present then it is ignored`() {
        val decoded = decode(
            """
            {
              "barcode": "04963406",
              "category": "OTHER",
              "source": "OFF",
              "nutrients": {"per100g": {"sodium": 1.0}, "perPack": {}}
            }
            """.trimIndent(),
        )

        assertEquals(expected = minimal, actual = decoded)
    }

    private fun encode(product: ProductResponse): String =
        ApiJson.encodeToString(serializer = ProductResponse.serializer(), value = product)

    private fun decode(json: String): ProductResponse =
        ApiJson.decodeFromString(deserializer = ProductResponse.serializer(), string = json)
}
