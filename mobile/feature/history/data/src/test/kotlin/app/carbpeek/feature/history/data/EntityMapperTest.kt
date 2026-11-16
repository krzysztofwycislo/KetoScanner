package app.carbpeek.feature.history.data

import app.carbpeek.core.database.history.ProductAnalysisHistoryEntryEntity
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.MacronutrientAnalysisResult
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.preferences.api.model.DietMode
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class EntityMapperTest {

    @Test
    fun `when each diet rate is analysed then the entry has that rate`() {
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
        val mapped = entity().toDomain(ProductAnalysis.InsufficientData(productWithCarbs))

        assertNull(mapped.dietRate)
        assertEquals(CARBS, mapped.carbsPer100g)
    }

    @Test
    fun `when analysis has a category then it replaces the stored one`() {
        val mapped = entity(category = "YOGURT_AND_CREAM").toDomain(ProductAnalysis.Rated(product, macronutrients))

        assertEquals(ProductCategory.CHEESE, mapped.category)
    }

    @Test
    fun `when entity is mapped then id name barcode brand update time and carbs are kept`() {
        val entity = entity().apply {
            id = ID
            updateTime = UPDATE_TIME
        }

        val entry = entity.toDomain(ProductAnalysis.Rated(productWithCarbs, macronutrients))

        assertEquals(ID, entry.id)
        assertEquals(NAME, entry.productName)
        assertEquals(BARCODE, entry.productBarcode)
        assertEquals(BRAND, entry.productBrand)
        assertEquals(UPDATE_TIME, entry.updateTime)
        assertEquals(CARBS, entry.carbsPer100g)
    }

    @Test
    fun `when product is mapped to entity then name barcode brand and category are kept`() {
        val entity = product.toEntity()

        assertEquals(NAME, entity.productName)
        assertEquals(BARCODE, entity.productBarcode)
        assertEquals(BRAND, entity.productBrand)
        assertEquals("CHEESE", entity.category)
    }

    @Test
    fun `when entity list is mapped then order is kept and each row gets its own analysis`() {
        val entities = listOf("1", "2", "3").map { ProductAnalysisHistoryEntryEntity(NAME, it, BRAND, null) }
        val analyses = mapOf(
            Barcode("2") to ProductAnalysis.Rated(product, macronutrients.copy(rate = DietRate.NEUTRAL)),
        )

        val mapped = entities.toDomain(analyses)

        assertEquals(listOf("1", "2", "3"), mapped.map { it.productBarcode })
        assertEquals(listOf(null, DietRate.NEUTRAL, null), mapped.map { it.dietRate })
    }

    @Test
    fun `when stored category is unknown or null and there is no analysis then domain category is other`() {
        assertEquals(ProductCategory.OTHER, entity(category = "SOUPS").toDomain(null).category)
        assertEquals(ProductCategory.OTHER, entity(category = null).toDomain(null).category)
    }

    private companion object {
        const val ID = 42L
        const val NAME = "Peanut butter"
        const val BARCODE = "5900000000001"
        const val BRAND = "Nuts & Co"
        const val CARBS = 12.5
        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)

        val macronutrients = MacronutrientAnalysisResult(
            dietMode = DietMode.KETO,
            rate = DietRate.GOOD,
            reasons = emptyList(),
            carbsPer100g = 5.0,
            dailyCarbConsumption = 40,
        )

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

        fun entity(category: String? = null) = ProductAnalysisHistoryEntryEntity(NAME, BARCODE, BRAND, category)
    }
}
