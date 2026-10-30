package pl.handsome.club.feature.history.data

import pl.handsome.club.core.database.history.ProductAnalysisHistoryEntryEntity
import pl.handsome.club.core.model.ProductCategory
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.preferences.api.model.DietMode
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class EntityMapperTest {

    @Test
    fun `when each diet rate goes through entity and back then the rate is unchanged`() {
        DietRate.entries.forEach { dietRate ->
            val mapped = ProductAnalysis.Rated(product, analysis.copy(rate = dietRate)).toEntity().toDomain()

            assertEquals(dietRate, mapped.dietRate)
        }
    }

    @Test
    fun `when stored rate is null then domain rate is null`() {
        val entity = ProductAnalysisHistoryEntryEntity(NAME, BARCODE, BRAND, null, null, null)

        assertNull(entity.toDomain().dietRate)
    }

    @Test
    fun `when stored rate name is unknown then domain rate is null`() {
        val entity = ProductAnalysisHistoryEntryEntity(NAME, BARCODE, BRAND, "VERY_GOOD", null, null)

        assertNull(entity.toDomain().dietRate)
    }

    @Test
    fun `when entity is mapped then id name barcode brand update time and carbs are kept`() {
        val entity = ProductAnalysisHistoryEntryEntity(NAME, BARCODE, BRAND, null, CARBS, null).apply {
            id = ID
            updateTime = UPDATE_TIME
        }

        val entry = entity.toDomain()

        assertEquals(ID, entry.id)
        assertEquals(NAME, entry.productName)
        assertEquals(BARCODE, entry.productBarcode)
        assertEquals(BRAND, entry.productBrand)
        assertEquals(UPDATE_TIME, entry.updateTime)
        assertEquals(CARBS, entry.carbsPer100g)
    }

    @Test
    fun `when analysis result is mapped to entity then product fields and rate name are kept`() {
        val entity = ProductAnalysis.Rated(product, analysis).toEntity()

        assertEquals(NAME, entity.productName)
        assertEquals(BARCODE, entity.productBarcode)
        assertEquals(BRAND, entity.productBrand)
        assertEquals("GOOD", entity.dietRate)
    }

    @Test
    fun `when rated analysis is mapped to entity then carbs per 100 g are kept`() {
        val entity = ProductAnalysis.Rated(productWithCarbs, analysis).toEntity()

        assertEquals(CARBS, entity.carbsPer100g)
    }

    @Test
    fun `when insufficient data analysis is mapped to entity then rate is null`() {
        val entity = ProductAnalysis.InsufficientData(product).toEntity()

        assertEquals(BARCODE, entity.productBarcode)
        assertNull(entity.dietRate)
    }

    @Test
    fun `when insufficient data analysis has carbs then entity keeps the carbs and a null rate`() {
        val entity = ProductAnalysis.InsufficientData(productWithCarbs).toEntity()

        assertEquals(CARBS, entity.carbsPer100g)
        assertNull(entity.dietRate)
    }

    @Test
    fun `when product has no carbs then entity carbs are null`() {
        val entity = ProductAnalysis.Rated(product, analysis).toEntity()

        assertNull(entity.carbsPer100g)
    }

    @Test
    fun `when entity list is mapped then order is kept`() {
        val entities = listOf("1", "2", "3").map {
            ProductAnalysisHistoryEntryEntity(NAME, it, BRAND, null, null, null)
        }

        assertEquals(listOf("1", "2", "3"), entities.toDomain().map { it.productBarcode })
    }

    @Test
    fun `when analysis goes through entity and back then the product category is kept`() {
        val entity = ProductAnalysis.Rated(product, analysis).toEntity()

        assertEquals("CHEESE", entity.category)
        assertEquals(ProductCategory.CHEESE, entity.toDomain().category)
    }

    @Test
    fun `when stored category is unknown or null then domain category is other`() {
        val unknown = ProductAnalysisHistoryEntryEntity(NAME, BARCODE, BRAND, null, null, "SOUPS")
        val missing = ProductAnalysisHistoryEntryEntity(NAME, BARCODE, BRAND, null, null, null)

        assertEquals(ProductCategory.OTHER, unknown.toDomain().category)
        assertEquals(ProductCategory.OTHER, missing.toDomain().category)
    }

    private companion object {
        const val ID = 42L
        const val NAME = "Peanut butter"
        const val BARCODE = "5900000000001"
        const val BRAND = "Nuts & Co"
        const val CARBS = 12.5
        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)

        val analysis = MacronutrientAnalysisResult(DietMode.KETO, DietRate.GOOD, emptyList(), 5.0, 800.0, 40)

        val product = Product(
            name = NAME,
            barcode = BARCODE,
            brand = BRAND,
            nutriments = ProductNutriments(null, null, null, null, null, null, null, null, null, null),
            imageUrl = null,
            servingAmount = null,
            category = ProductCategory.CHEESE,
        )

        val productWithCarbs = product.copy(
            nutriments = product.nutriments.copy(carbohydratesPer100g = CARBS),
        )
    }
}
