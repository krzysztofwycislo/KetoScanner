package app.carbpeek.feature.analysis.domain

import app.carbpeek.core.common.Outcome
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.testing.FakeErrorReporter
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.analysis.api.model.dietRate
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.product.api.ProductRepository
import app.carbpeek.feature.product.api.model.Ingredient
import app.carbpeek.feature.product.api.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NoDietAnalysisTest {

    private val engine = DietAnalysisEngine()
    private val products = FakeProductRepository()
    private val preferences = FakePreferencesRepository()
    private val errorReporter = FakeErrorReporter()

    @Test
    fun `when there is no diet then a complete product is unrated and keeps product and ingredients`() {
        val analysis = engine.analyze(preferences = DietPreferences.NoDiet, product = productWithIngredients)

        assertEquals(
            ProductAnalysis.Unrated(
                product = productWithIngredients,
                ingredients = assertNotNull(IngredientsAnalyzer.analyze(productWithIngredients.ingredients)),
            ),
            analysis,
        )
        assertFalse(analysis.hasDiet)
        assertNull(analysis.dietRate)
    }

    @Test
    fun `when there is no diet and carbs fat or protein are missing then it is insufficient data without a diet`() {
        incompleteProducts.forEach { product ->
            assertEquals(
                ProductAnalysis.InsufficientData(
                    product = product,
                    ingredients = IngredientsAnalyzer.analyze(product.ingredients),
                    hasDiet = false,
                ),
                engine.analyze(preferences = DietPreferences.NoDiet, product = product),
            )
        }
    }

    @Test
    fun `when there is a diet then rated and insufficient data have a diet`() {
        DietPreset.entries.forEach { preset ->
            val rated = engine.analyze(preferences = preset.preferences, product = productWithIngredients)
            assertIs<ProductAnalysis.Rated>(rated)
            assertTrue(rated.hasDiet)

            incompleteProducts.forEach { product ->
                val incomplete = engine.analyze(preferences = preset.preferences, product = product)
                assertIs<ProductAnalysis.InsufficientData>(incomplete)
                assertTrue(incomplete.hasDiet)
            }
        }
    }

    @Test
    fun `when no diet is chosen and a diet is back then the open analysis follows without a new product`() = runTest {
        val analyzeProduct = AnalyzeProductImpl(
            products = products,
            preferences = preferences,
            engine = engine,
            errorReporter = errorReporter,
        )
        val analyses = mutableListOf<ProductAnalysis>()
        backgroundScope.launch { analyzeProduct(productWithIngredients.barcode).toList(analyses) }
        runCurrent()

        preferences.current.value = DietPreferences.NoDiet
        runCurrent()
        preferences.current.value = DietPreset.KETO.preferences
        runCurrent()

        assertEquals(listOf(DietRate.GOOD, null, DietRate.GOOD), analyses.map { it.dietRate })
        assertIs<ProductAnalysis.Unrated>(analyses[1])
        assertEquals(analyses.first(), analyses.last())
        assertEquals(1, products.requests)
    }

    @Test
    fun `when no diet is chosen and a diet is back then the list analyses follow without new products`() = runTest {
        val analyzeProducts = AnalyzeProductsImpl(
            products = products,
            preferences = preferences,
            engine = engine,
            errorReporter = errorReporter,
        )
        val barcode = Barcode(productWithIngredients.barcode)
        val emissions = mutableListOf<Map<Barcode, ProductAnalysis>>()
        backgroundScope.launch { analyzeProducts(setOf(barcode)).toList(emissions) }
        runCurrent()

        preferences.current.value = DietPreferences.NoDiet
        runCurrent()
        preferences.current.value = DietPreset.KETO.preferences
        runCurrent()

        val analyses = emissions.map { it.getValue(barcode) }
        assertEquals(listOf(DietRate.GOOD, null, DietRate.GOOD), analyses.map { it.dietRate })
        assertIs<ProductAnalysis.Unrated>(analyses[1])
        assertEquals(analyses.first(), analyses.last())
        assertEquals(1, products.requests)
    }

    private class FakeProductRepository : ProductRepository {

        var requests = 0

        override fun observeProduct(barcode: String): Flow<Product> {
            requests++
            return flowOf(productWithIngredients)
        }

        override fun observeProducts(barcodes: Set<Barcode>): Flow<List<Product>> {
            requests++
            return flowOf(listOf(productWithIngredients))
        }

        override suspend fun refreshProduct(barcode: String): Outcome<Unit> = throw NotImplementedError()
    }

    private class FakePreferencesRepository : DietPreferencesRepository {

        val current = MutableStateFlow(DietPreset.KETO.preferences)

        override fun observeDietPreferences(): Flow<DietPreferences> = current

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = throw NotImplementedError()

        override suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit> =
            throw NotImplementedError()
    }

    private companion object {
        val completeNutriments = macroNutriments(
            fatsPer100g = 20.0,
            proteinsPer100g = 10.0,
            carbohydratesPer100g = 2.0,
        )

        val productWithIngredients = createProduct(
            name = "Cream cheese",
            productNutriments = completeNutriments,
        ).copy(
            ingredients = listOf(
                Ingredient(
                    id = "en:sugar",
                    name = "sugar",
                    labelName = "sugar",
                    declaredPercent = null,
                    subIngredients = emptyList(),
                ),
            ),
        )

        val incompleteProducts = listOf(
            completeNutriments.copy(carbohydratesPer100g = null),
            completeNutriments.copy(fatsPer100g = null),
            completeNutriments.copy(proteinsPer100g = null),
        ).map { productWithIngredients.copy(nutriments = it) }
    }
}
