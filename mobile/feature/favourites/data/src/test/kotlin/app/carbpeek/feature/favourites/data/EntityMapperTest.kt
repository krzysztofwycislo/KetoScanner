package app.carbpeek.feature.favourites.data

import app.carbpeek.core.database.favourite.FavouriteProductEntity
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.favourites.api.model.FavouriteProduct
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class EntityMapperTest {

    @Test
    fun `when entity is mapped with an analysis then stored fields and live fields are set`() {
        val entity = FavouriteProductEntity(BARCODE, NAME, BRAND, IMAGE_URL, "YOGURT_AND_CREAM").apply {
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
            entity.toDomain(ProductAnalysis.Rated(product, macronutrients)),
        )
    }

    @Test
    fun `when each diet rate is analysed then the favourite has that rate`() {
        DietRate.entries.forEach { dietRate ->
            val mapped = entity().toDomain(ProductAnalysis.Rated(product, macronutrients.copy(rate = dietRate)))

            assertEquals(dietRate, mapped.dietRate)
        }
    }

    @Test
    fun `when there is no analysis then rate and carbs are null and the stored category is kept`() {
        val mapped = entity(category = "YOGURT_AND_CREAM").toDomain(null)

        assertNull(mapped.dietRate)
        assertNull(mapped.carbsPer100g)
        assertEquals(ProductCategory.YOGURT_AND_CREAM, mapped.category)
    }

    @Test
    fun `when analysis is insufficient data then the rate is null and the product carbs are kept`() {
        val mapped = entity().toDomain(ProductAnalysis.InsufficientData(product))

        assertNull(mapped.dietRate)
        assertEquals(CARBS, mapped.carbsPer100g)
    }

    @Test
    fun `when analysis is unrated then the rate is null and the product carbs are kept`() {
        val mapped = entity().toDomain(ProductAnalysis.Unrated(product))

        assertNull(mapped.dietRate)
        assertEquals(CARBS, mapped.carbsPer100g)
    }

    @Test
    fun `when entity list is mapped then order is kept and each row gets its own analysis`() {
        val entities = listOf("1", "2", "3").map { FavouriteProductEntity(it, NAME, BRAND, null, null) }
        val analyses = mapOf(
            Barcode("2") to ProductAnalysis.Rated(product, macronutrients.copy(rate = DietRate.NEUTRAL)),
        )

        val mapped = entities.toDomain(analyses)

        assertEquals(listOf("1", "2", "3"), mapped.map { it.productBarcode })
        assertEquals(listOf(null, DietRate.NEUTRAL, null), mapped.map { it.dietRate })
    }

    @Test
    fun `when product is mapped to entity then barcode name brand image and category are kept`() {
        val entity = product.toFavouriteEntity()

        assertEquals(BARCODE, entity.productBarcode)
        assertEquals(NAME, entity.productName)
        assertEquals(BRAND, entity.productBrand)
        assertEquals(IMAGE_URL, entity.imageUrl)
        assertEquals("CHEESE", entity.category)
    }

    @Test
    fun `when stored category is unknown or null and there is no analysis then domain category is other`() {
        assertEquals(ProductCategory.OTHER, entity(category = "SOUPS").toDomain(null).category)
        assertEquals(ProductCategory.OTHER, entity(category = null).toDomain(null).category)
    }

    private companion object {
        const val BARCODE = "5900000000001"
        const val NAME = "Peanut butter"
        const val BRAND = "Nuts & Co"
        const val IMAGE_URL = "https://example.com/image.jpg"
        const val CARBS = 12.5
        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)

        val macronutrients = MacronutrientAnalysisResult(
            dietMode = DietMode.KETO,
            rate = DietRate.GOOD,
            reasons = emptyList(),
            carbsPer100g = CARBS,
            dailyCarbConsumption = 40,
        )

        val product = Product(
            name = NAME,
            barcode = BARCODE,
            brand = BRAND,
            nutriments = ProductNutriments(null, null, null, null, null, null, null, CARBS, null, null),
            imageUrl = IMAGE_URL,
            servingAmount = null,
            category = ProductCategory.CHEESE,
        )

        fun entity(category: String? = null) = FavouriteProductEntity(BARCODE, NAME, BRAND, null, category)
    }
}
