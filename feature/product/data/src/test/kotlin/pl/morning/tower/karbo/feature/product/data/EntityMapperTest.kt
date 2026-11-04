package pl.morning.tower.karbo.feature.product.data

import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.product.api.model.Product
import pl.morning.tower.karbo.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
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
