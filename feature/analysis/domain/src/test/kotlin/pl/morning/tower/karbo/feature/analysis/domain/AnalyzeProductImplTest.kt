package pl.morning.tower.karbo.feature.analysis.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.getOrThrow
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.core.testing.FakeErrorReporter
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.analysis.api.model.dietRate
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset
import pl.morning.tower.karbo.feature.product.api.ProductRepository
import pl.morning.tower.karbo.feature.product.api.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AnalyzeProductImplTest {

    private val products = FakeProductRepository()
    private val preferences = FakePreferencesRepository()
    private val errorReporter = FakeErrorReporter()
    private val analyzeProduct = AnalyzeProductImpl(products, preferences, DietAnalysisEngine(), errorReporter)

    @Test
    fun `when product has carbs then it is rated using the current preferences`() = runTest {
        products.products = listOf(productWithCarbs)
        preferences.current = Outcome.Success(DietPreferences(maxDailyCarbsAmount = 15))

        val analysis = analyzeProduct(BARCODE).first()

        val rated = assertIs<ProductAnalysis.Rated>(analysis)
        assertEquals(productWithCarbs, rated.product)
        assertEquals(15, rated.macronutrients.dailyCarbConsumption)
        assertEquals(DietRate.NOT_ADVISED, rated.macronutrients.rate)
        assertEquals(listOf(BARCODE), products.requestedBarcodes)
    }

    @Test
    fun `when reading preferences fails then the product is rated using the default preferences`() = runTest {
        products.products = listOf(productWithCarbs)
        preferences.current = Outcome.Failure(AppError.Storage)

        val analysis = analyzeProduct(BARCODE).first()

        val rated = assertIs<ProductAnalysis.Rated>(analysis)
        assertEquals(DietPreferences.Default.maxDailyCarbsAmount, rated.macronutrients.dailyCarbConsumption)
        assertEquals(DietMode.KETO, rated.macronutrients.dietMode)
        assertEquals(listOf(preferencesFailureReport), errorReporter.reports)
    }

    @Test
    fun `when low carb mode is preferred then the result is a low carb result`() = runTest {
        products.products = listOf(productWithCarbs)
        preferences.current = Outcome.Success(DietPreferences(40, DietMode.LOW_CARB))

        val analysis = analyzeProduct(BARCODE).first()

        assertEquals(DietMode.LOW_CARB, assertIs<ProductAnalysis.Rated>(analysis).macronutrients.dietMode)
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
        val updates = MutableStateFlow(productWithCarbs)
        products.updates = updates
        val analyses = mutableListOf<ProductAnalysis>()
        backgroundScope.launch { analyzeProduct(BARCODE).toList(analyses) }
        runCurrent()

        updates.value = changed
        runCurrent()

        assertEquals(2, analyses.size)
        assertIs<ProductAnalysis.Rated>(analyses[0])
        assertEquals(ProductAnalysis.InsufficientData(changed), analyses[1])
    }

    @Test
    fun `when the profile changes then the open analysis is re-rated without a new product emission`() = runTest {
        val hummus = createProduct("Hummus", macroNutriments(20.0, 7.0, 12.0, 0.5), null)
        products.products = listOf(hummus)
        preferences.current = Outcome.Success(DietPreset.KETO.preferences)
        val analyses = mutableListOf<ProductAnalysis>()
        backgroundScope.launch { analyzeProduct(BARCODE).toList(analyses) }
        runCurrent()

        preferences.current = Outcome.Success(DietPreset.LOW_CARB.preferences)
        runCurrent()

        assertEquals(listOf(DietRate.NEUTRAL, DietRate.GOOD), analyses.map { it.dietRate })
        assertEquals(listOf(BARCODE), products.requestedBarcodes)
    }

    @Test
    fun `when observing the product throws then the exception propagates`() = runTest {
        val failure = IllegalStateException("disk")
        products.observeFailure = failure
        var caught: Throwable? = null

        analyzeProduct(BARCODE).catch { caught = it }.toList()

        assertEquals(failure.message, assertIs<IllegalStateException>(caught).message)
    }

    @Test
    fun `when observing preferences throws an unexpected exception then it propagates`() = runTest {
        products.products = listOf(productWithCarbs)
        val failure = IllegalStateException("bug")
        preferences.observeFailure = failure
        var caught: Throwable? = null

        analyzeProduct(BARCODE).catch { caught = it }.toList()

        assertEquals(failure.message, assertIs<IllegalStateException>(caught).message)
    }

    private class FakeProductRepository : ProductRepository {

        var products: List<Product> = emptyList()
        var updates: Flow<Product>? = null
        var observeFailure: Throwable? = null
        var refreshResult: Outcome<Unit> = Outcome.Success(Unit)
        val requestedBarcodes = mutableListOf<String>()

        override fun observeProduct(barcode: String): Flow<Product> {
            requestedBarcodes += barcode
            val failure = observeFailure ?: return updates ?: flowOf(*products.toTypedArray())
            return flow { throw failure }
        }

        override fun observeProducts(barcodes: Set<Barcode>): Flow<List<Product>> =
            throw NotImplementedError()

        override suspend fun refreshProduct(barcode: String): Outcome<Unit> = refreshResult
    }

    private class FakePreferencesRepository : DietPreferencesRepository {

        private val updates = MutableStateFlow<Outcome<DietPreferences>>(Outcome.Success(exampleDietPreferences))

        var current: Outcome<DietPreferences>
            get() = updates.value
            set(value) {
                updates.value = value
            }

        var observeFailure: Throwable? = null

        override fun observeDietPreferences(): Flow<DietPreferences> {
            val failure = observeFailure ?: return updates.map { it.getOrThrow() }
            return flow { throw failure }
        }

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = current

        override suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit> {
            current = Outcome.Success(preferences)
            return Outcome.Success(Unit)
        }
    }

    private companion object {
        const val BARCODE = "5900000000001"

        val productWithCarbs = createProduct(exampleNutrients)
    }
}
