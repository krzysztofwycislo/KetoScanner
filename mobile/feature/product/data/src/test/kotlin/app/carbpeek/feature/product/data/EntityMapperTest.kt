package app.carbpeek.feature.product.data

import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.product.api.model.Ingredient
import app.carbpeek.feature.product.api.model.NovaGroup
import app.carbpeek.feature.product.api.model.NutriScoreGrade
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class EntityMapperTest {

    @Test
    fun `when product has every field set then it survives the round trip`() {
        val entity = fullProduct.toEntity(FETCHED_AT)

        assertEquals(fullProduct, entity.toDomain())
        assertEquals(FETCHED_AT, entity.fetchedAt)
    }

    @Test
    fun `when product has all nullable fields null then it survives the round trip`() {
        val entity = emptyProduct.toEntity(FETCHED_AT)

        assertEquals(emptyProduct, entity.toDomain())
        assertEquals(FETCHED_AT, entity.fetchedAt)
    }

    @Test
    fun `when stored category is unknown or null then product category is other`() {
        val entity = fullProduct.toEntity(FETCHED_AT)

        assertEquals(ProductCategory.OTHER, entity.copy(category = "SOUPS").toDomain().category)
        assertEquals(ProductCategory.OTHER, entity.copy(category = null).toDomain().category)
    }

    @Test
    fun `when stored nutri score is unknown then product nutri score is null`() {
        val entity = fullProduct.toEntity(FETCHED_AT)

        assertNull(entity.copy(nutriScore = "F").toDomain().nutriScore)
    }

    @Test
    fun `when product has no scores or ingredients then the columns are null`() {
        val entity = emptyProduct.toEntity(FETCHED_AT)

        assertNull(entity.ingredients)
        assertNull(entity.nutriScore)
        assertNull(entity.novaGroup)
    }

    @Test
    fun `when stored ingredients use the old nested key then they are read without nested ingredients`() {
        val json = """[{"id":"en:onion","name":"onion","sub":[{"id":"en:sugar","name":"sugar"}]}]"""

        val ingredient = json.decodeIngredients().single()

        assertEquals("onion", ingredient.name)
        assertEquals(emptyList(), ingredient.subIngredients)
    }

    private companion object {
        val FETCHED_AT = Instant.fromEpochMilliseconds(1_700_000_000_000)

        val fullProduct = Product(
            name = "Nutella",
            barcode = "3017620422003",
            brand = "Ferrero",
            nutriments = ProductNutriments(
                80.0,
                539.0,
                4.6,
                30.9,
                0.9,
                6.3,
                8.6,
                57.5,
                0.02,
                0.107,
                8.4,
                56.3,
                0.6,
                4.0,
            ),
            imageUrl = "https://images.openfoodfacts.org/front.jpg",
            servingAmount = "15",
            category = ProductCategory.SPREADS,
            ingredients = listOf(
                Ingredient(
                    id = "en:sugar",
                    name = "Sugar",
                    labelName = "Sugar",
                    declaredPercent = null,
                    subIngredients = emptyList(),
                ),
                Ingredient(
                    id = "en:hazelnut",
                    name = "Hazelnuts",
                    labelName = "Hazelnuts",
                    declaredPercent = 13.0,
                    subIngredients = emptyList(),
                ),
                Ingredient(
                    id = "en:milk-chocolate",
                    name = "Milk chocolate",
                    labelName = "czekolada mleczna",
                    declaredPercent = 8.7,
                    subIngredients = listOf(
                        Ingredient(
                            id = "en:sugar",
                            name = "sugar",
                            labelName = "sugar",
                            declaredPercent = null,
                            subIngredients = emptyList(),
                        ),
                    ),
                ),
            ),
            nutriScore = NutriScoreGrade.E,
            novaGroup = NovaGroup.ULTRA_PROCESSED,
        )

        val emptyProduct = Product(
            name = "",
            barcode = "5900000000001",
            brand = "",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, null, null, null),
            imageUrl = null,
            servingAmount = null,
            category = ProductCategory.OTHER,
        )
    }
}
