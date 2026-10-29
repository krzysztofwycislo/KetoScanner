package pl.handsome.club.feature.analysis.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.analysis.api.model.DietRate
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.ProductRepository
import pl.handsome.club.feature.product.api.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class AnalyzeProductImplTest {

    private val products = FakeProductRepository()
    private val preferences = FakePreferencesRepository()
    private val analyzeProduct = AnalyzeProductImpl(products, preferences, DietAnalysisEngine())

    @Test
    fun `when product has carbs then it is rated using the current preferences`() = runTest {
        products.products = listOf(productWithCarbs)
        preferences.current = DietPreferences(maxDailyCarbsAmount = 15)

        val analysis = analyzeProduct(BARCODE).first()

        val rated = assertIs<ProductAnalysis.Rated>(analysis)
        assertEquals(productWithCarbs, rated.product)
        assertEquals(15, rated.macronutrients.dailyCarbConsumption)
        assertEquals(DietRate.NOT_ADVISED, rated.macronutrients.carbsRate)
        assertEquals(listOf(BARCODE), products.requestedBarcodes)
    }

    @Test
    fun `when product has no carbs then analysis is insufficient data`() = runTest {
        val product = createProduct(exampleNutrients.copy(carbohydratesPer100g = null))
        products.products = listOf(product)

        assertEquals(ProductAnalysis.InsufficientData(product), analyzeProduct(BARCODE).first())
    }

    @Test
    fun `when product is emitted twice then it is analysed twice`() = runTest {
        val changed = createProduct(exampleNutrients.copy(carbohydratesPer100g = null))
        products.products = listOf(productWithCarbs, changed)

        val analyses = analyzeProduct(BARCODE).toList()

        assertEquals(2, analyses.size)
        assertIs<ProductAnalysis.Rated>(analyses[0])
        assertEquals(ProductAnalysis.InsufficientData(changed), analyses[1])
    }

    @Test
    fun `when observing the product throws then the exception propagates`() = runTest {
        val failure = IllegalStateException("disk")
        products.observeFailure = failure
        var caught: Throwable? = null

        analyzeProduct(BARCODE).catch { caught = it }.toList()

        assertSame(failure, caught)
    }

    private class FakeProductRepository : ProductRepository {

        var products: List<Product> = emptyList()
        var observeFailure: Throwable? = null
        var refreshResult: Outcome<Unit> = Outcome.Success(Unit)
        val requestedBarcodes = mutableListOf<String>()

        override fun observeProduct(barcode: String): Flow<Product> {
            requestedBarcodes += barcode
            val failure = observeFailure ?: return flowOf(*products.toTypedArray())
            return flow { throw failure }
        }

        override suspend fun refreshProduct(barcode: String): Outcome<Unit> = refreshResult
    }

    private class FakePreferencesRepository : DietPreferencesRepository {

        var current: DietPreferences = exampleDietPreferences

        override fun observeDietPreferences(): Flow<DietPreferences> = flowOf(current)

        override suspend fun getDietPreferences(): DietPreferences = current

        override suspend fun setMaxDailyCarbsAmount(amount: Int) {
            current = DietPreferences(amount)
        }
    }

    private companion object {
        const val BARCODE = "5900000000001"

        val productWithCarbs = createProduct(exampleNutrients)
    }
}
