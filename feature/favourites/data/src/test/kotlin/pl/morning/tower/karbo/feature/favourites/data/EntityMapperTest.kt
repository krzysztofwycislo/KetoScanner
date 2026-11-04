package pl.morning.tower.karbo.feature.favourites.data

import pl.morning.tower.karbo.core.database.favourite.FavouriteProductEntity
import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.favourites.api.model.FavouriteProduct
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.product.api.model.Product
import pl.morning.tower.karbo.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class EntityMapperTest {

    @Test
    fun `when entity is mapped then all fields are kept`() {
        val entity = FavouriteProductEntity(BARCODE, NAME, BRAND, IMAGE_URL, "GOOD", CARBS, "CHEESE").apply {
            updateTime = UPDATE_TIME
        }

        assertEquals(
            FavouriteProduct(
                BARCODE,
                NAME,
                BRAND,
                UPDATE_TIME,
                IMAGE_URL,
                DietRate.GOOD,
                CARBS,
                ProductCategory.CHEESE,
            ),
            entity.toDomain(),
        )
    }

    @Test
    fun `when each diet rate goes through entity and back then the rate is unchanged`() {
        DietRate.entries.forEach { dietRate ->
            val mapped = ProductAnalysis.Rated(product, analysis.copy(rate = dietRate)).toFavouriteEntity().toDomain()

            assertEquals(dietRate, mapped.dietRate)
        }
    }

    @Test
    fun `when stored rate is null then domain rate is null`() {
        val entity = FavouriteProductEntity(BARCODE, NAME, BRAND, null, null, null, null)

        assertNull(entity.toDomain().dietRate)
    }

    @Test
    fun `when stored rate name is unknown then domain rate is null`() {
        val entity = FavouriteProductEntity(BARCODE, NAME, BRAND, null, "VERY_GOOD", null, null)

        assertNull(entity.toDomain().dietRate)
    }

    @Test
    fun `when entity list is mapped then order is kept`() {
        val entities = listOf("1", "2", "3").map { FavouriteProductEntity(it, NAME, BRAND, null, null, null, null) }

        assertEquals(listOf("1", "2", "3"), entities.toDomain().map { it.productBarcode })
    }

    @Test
    fun `when product is mapped to entity then barcode name brand and image are kept and rating is empty`() {
        val entity = product.toFavouriteEntity()

        assertEquals(BARCODE, entity.productBarcode)
        assertEquals(NAME, entity.productName)
        assertEquals(BRAND, entity.productBrand)
        assertEquals(IMAGE_URL, entity.imageUrl)
        assertNull(entity.dietRate)
        assertNull(entity.carbsPer100g)
    }

    @Test
    fun `when rated analysis is mapped to entity then product fields rate name and carbs are kept`() {
        val entity = ProductAnalysis.Rated(product, analysis).toFavouriteEntity()

        assertEquals(BARCODE, entity.productBarcode)
        assertEquals(NAME, entity.productName)
        assertEquals(BRAND, entity.productBrand)
        assertEquals(IMAGE_URL, entity.imageUrl)
        assertEquals("GOOD", entity.dietRate)
        assertEquals(CARBS, entity.carbsPer100g)
    }

    @Test
    fun `when insufficient data analysis is mapped to entity then rate is null and carbs are kept`() {
        val entity = ProductAnalysis.InsufficientData(product).toFavouriteEntity()

        assertNull(entity.dietRate)
        assertEquals(CARBS, entity.carbsPer100g)
    }

    @Test
    fun `when product goes through entity and back then the category is kept`() {
        val entity = ProductAnalysis.Rated(product, analysis).toFavouriteEntity()

        assertEquals("CHEESE", entity.category)
        assertEquals(ProductCategory.CHEESE, entity.toDomain().category)
        assertEquals("CHEESE", product.toFavouriteEntity().category)
    }

    @Test
    fun `when stored category is unknown or null then domain category is other`() {
        val unknown = FavouriteProductEntity(BARCODE, NAME, BRAND, null, null, null, "SOUPS")
        val missing = FavouriteProductEntity(BARCODE, NAME, BRAND, null, null, null, null)

        assertEquals(ProductCategory.OTHER, unknown.toDomain().category)
        assertEquals(ProductCategory.OTHER, missing.toDomain().category)
    }

    private companion object {
        const val BARCODE = "5900000000001"
        const val NAME = "Peanut butter"
        const val BRAND = "Nuts & Co"
        const val IMAGE_URL = "https://example.com/image.jpg"
        const val CARBS = 12.5
        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)

        val analysis = MacronutrientAnalysisResult(DietMode.KETO, DietRate.GOOD, emptyList(), CARBS, 800.0, 40)

        val product = Product(
            name = NAME,
            barcode = BARCODE,
            brand = BRAND,
            nutriments = ProductNutriments(null, null, null, null, null, null, null, CARBS, null, null),
            imageUrl = IMAGE_URL,
            servingAmount = null,
            category = ProductCategory.CHEESE,
        )
    }
}
