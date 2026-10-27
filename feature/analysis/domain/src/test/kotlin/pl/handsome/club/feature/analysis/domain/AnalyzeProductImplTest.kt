package pl.handsome.club.feature.analysis.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import pl.handsome.club.core.common.AppError
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

class AnalyzeProductImplTest {

    private val products = FakeProductRepository()
    private val preferences = FakePreferencesRepository()
    private val analyzeProduct = AnalyzeProductImpl(products, preferences, DietAnalysisEngine())

    @Test
    fun `when product has carbs then it is rated using the current preferences`() = runTest {
        products.result = Outcome.Success(productWithCarbs)
        preferences.current = DietPreferences(maxDailyCarbsAmount = 15)

        val outcome = analyzeProduct(BARCODE)

        val rated = assertIs<ProductAnalysis.Rated>(assertIs<Outcome.Success<ProductAnalysis>>(outcome).value)
        assertEquals(productWithCarbs, rated.product)
        assertEquals(15, rated.macronutrients.dailyCarbConsumption)
        assertEquals(DietRate.NOT_ADVISED, rated.macronutrients.carbsRate)
        assertEquals(listOf(BARCODE), products.requestedBarcodes)
    }

    @Test
    fun `when product has no carbs then analysis is insufficient data`() = runTest {
        val product = createProduct(exampleNutrients.copy(carbohydratesPer100g = null))
        products.result = Outcome.Success(product)

        assertEquals(Outcome.Success(ProductAnalysis.InsufficientData(product)), analyzeProduct(BARCODE))
    }

    @Test
    fun `when product is not found then not found is passed through`() = runTest {
        products.result = Outcome.Failure(AppError.NotFound)

        assertEquals(Outcome.Failure(AppError.NotFound), analyzeProduct(BARCODE))
    }

    @Test
    fun `when network fails then network failure is passed through`() = runTest {
        products.result = Outcome.Failure(AppError.Network)

        assertEquals(Outcome.Failure(AppError.Network), analyzeProduct(BARCODE))
    }

    private class FakeProductRepository : ProductRepository {

        var result: Outcome<Product> = Outcome.Failure(AppError.NotFound)
        val requestedBarcodes = mutableListOf<String>()

        override suspend fun getProduct(barcode: String): Outcome<Product> {
            requestedBarcodes += barcode
            return result
        }
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
