package pl.handsome.club.feature.history.data

import pl.handsome.club.core.database.history.MacronutrientAnalysisEmbedded
import pl.handsome.club.core.database.history.ProductAnalysisHistoryEntryEntity
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.MacronutrientAnalysisResult
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class EntityMapperTest {

    @Test
    fun `when each diet rate goes through entity and back then analysis is unchanged`() {
        DietRate.entries.forEach { dietRate ->
            val analysis = analysis.copy(carbsRate = dietRate)

            val mapped = ProductAnalysis.Rated(product, analysis).toEntity().toDomain()

            assertEquals(analysis, mapped.macronutrientAnalysisResult)
        }
    }

    @Test
    fun `when embedded analysis is null then domain analysis is null`() {
        val entity = ProductAnalysisHistoryEntryEntity(NAME, BARCODE, BRAND, null)

        assertNull(entity.toDomain().macronutrientAnalysisResult)
    }

    @Test
    fun `when carbs rate name is unknown then domain analysis is null`() {
        val embedded = MacronutrientAnalysisEmbedded("VERY_GOOD", 10, 50.0, 20)
        val entity = ProductAnalysisHistoryEntryEntity(NAME, BARCODE, BRAND, embedded)

        assertNull(entity.toDomain().macronutrientAnalysisResult)
    }

    @Test
    fun `when entity is mapped then id name barcode brand and update time are kept`() {
        val entity = ProductAnalysisHistoryEntryEntity(NAME, BARCODE, BRAND, null).apply {
            id = ID
            updateTime = UPDATE_TIME
        }

        val entry = entity.toDomain()

        assertEquals(ID, entry.id)
        assertEquals(NAME, entry.productName)
        assertEquals(BARCODE, entry.productBarcode)
        assertEquals(BRAND, entry.productBrand)
        assertEquals(UPDATE_TIME, entry.updateTime)
    }

    @Test
    fun `when analysis result is mapped to entity then product fields are kept`() {
        val entity = ProductAnalysis.Rated(product, analysis).toEntity()

        assertEquals(NAME, entity.productName)
        assertEquals(BARCODE, entity.productBarcode)
        assertEquals(BRAND, entity.productBrand)
        assertEquals(MacronutrientAnalysisEmbedded("GOOD", 10, 50.0, 20), entity.macronutrientAnalysisResult)
    }

    @Test
    fun `when insufficient data analysis is mapped to entity then embedded analysis is null`() {
        val entity = ProductAnalysis.InsufficientData(product).toEntity()

        assertEquals(BARCODE, entity.productBarcode)
        assertNull(entity.macronutrientAnalysisResult)
    }

    @Test
    fun `when entity list is mapped then order is kept`() {
        val entities = listOf("1", "2", "3").map { ProductAnalysisHistoryEntryEntity(NAME, it, BRAND, null) }

        assertEquals(listOf("1", "2", "3"), entities.toDomain().map { it.productBarcode })
    }

    private companion object {
        const val ID = 42L
        const val NAME = "Peanut butter"
        const val BARCODE = "5900000000001"
        const val BRAND = "Nuts & Co"
        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)

        val analysis = MacronutrientAnalysisResult(DietRate.GOOD, 10, 50.0, 20)

        val product = Product(
            name = NAME,
            barcode = BARCODE,
            brand = BRAND,
            nutriments = ProductNutriments(null, null, null, null, null, null, null, null, null, null),
            imageUrl = null,
            servingAmount = null,
        )
    }
}
