package app.carbpeek.feature.analysis.domain

import app.carbpeek.core.common.AppError
import app.carbpeek.core.common.AppErrorException
import app.carbpeek.core.common.Outcome
import app.carbpeek.core.common.getOrNull
import app.carbpeek.core.common.getOrThrow
import app.carbpeek.core.model.Barcode
import app.carbpeek.core.testing.FakeErrorReporter
import app.carbpeek.feature.analysis.api.model.DietRate
import app.carbpeek.feature.analysis.api.model.DietRate.GOOD
import app.carbpeek.feature.analysis.api.model.DietRate.NOT_ADVISED
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.analysis.api.model.dietRate
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.preferences.api.model.DietPreferences
import app.carbpeek.feature.preferences.api.model.DietPreset
import app.carbpeek.feature.product.api.ProductRepository
import app.carbpeek.feature.product.api.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnalyzeProductsImplTest {

    private val products = FakeProductRepository()
    private val preferences = FakePreferencesRepository()
    private val engine = DietAnalysisEngine()
    private val errorReporter = FakeErrorReporter()
    private val analyzeProducts = AnalyzeProductsImpl(products, preferences, engine, errorReporter)

    @Test
    fun `when the profile changes then each food gets the expected rate without resubscribing`() = runTest {
        products.rows.value = Outcome.Success(realProducts.map { it.product })
        val emissions = collect(realProducts.map { it.barcode }.toSet())

        DietPreset.entries.forEach { preset ->
            preferences.current.value = Outcome.Success(preset.preferences)
            runCurrent()

            val analyses = emissions.last()
            realProducts.forEach { food ->
                assertEquals(food.rates.getValue(preset), analyses[food.barcode]?.dietRate, "${food.name} in $preset")
            }
        }
        assertEquals(1, products.observedBarcodes.size)
    }

    @Test
    fun `when a list and a single analysis rate the same product then they agree`() = runTest {
        products.rows.value = Outcome.Success(realProducts.map { it.product })
        val analyzeProduct = AnalyzeProductImpl(products, preferences, engine, errorReporter)

        DietPreset.entries.forEach { preset ->
            preferences.current.value = Outcome.Success(preset.preferences)
            val analyses = analyzeProducts(realProducts.map { it.barcode }.toSet()).first()

            realProducts.forEach { food ->
                assertEquals(analyzeProduct(food.barcode.value).first(), analyses[food.barcode], food.name)
            }
        }
    }

    @Test
    fun `when a product has no fat value then it is insufficient data under every preset`() = runTest {
        val product = food(
            "No fat value",
            macroNutriments(fatsPer100g = null, proteinsPer100g = 7.0, carbohydratesPer100g = 12.0),
            serving = null,
        )
        products.rows.value = Outcome.Success(listOf(product))

        DietPreset.entries.forEach { preset ->
            preferences.current.value = Outcome.Success(preset.preferences)

            val analyses = analyzeProducts(setOf(Barcode(product.barcode))).first()

            assertEquals(
                mapOf(Barcode(product.barcode) to ProductAnalysis.InsufficientData(product)),
                analyses,
            )
        }
    }

    @Test
    fun `when a barcode has no row then it is absent and the other barcodes are rated`() = runTest {
        val hummus = realProducts.named(HUMMUS).product
        products.rows.value = Outcome.Success(listOf(hummus))

        val analyses = analyzeProducts(setOf(Barcode(hummus.barcode), Barcode(MISSING_BARCODE))).first()

        assertEquals(
            setOf(Barcode(hummus.barcode)),
            analyses.keys,
        )
    }

    @Test
    fun `when a product emission changes the nutrition then the rate is updated`() = runTest {
        val hummus = realProducts.named(HUMMUS).product
        val bread = realProducts.named("White bread (Jason's Sourdough)").product.copy(barcode = hummus.barcode)
        products.rows.value = Outcome.Success(listOf(hummus))
        val emissions = collect(setOf(Barcode(hummus.barcode)))

        products.rows.value = Outcome.Success(listOf(bread))
        runCurrent()

        assertEquals(listOf(GOOD, NOT_ADVISED), emissions.map { rateOf(it, hummus.barcode) })
    }

    @Test
    fun `when products fail then the same error is thrown`() = runTest {
        products.rows.value = Outcome.Failure(AppError.Storage)

        val exception = assertFailsWith<AppErrorException> { analyzeProducts(emptySet()).first() }

        assertEquals(AppError.Storage, exception.error)
    }

    @Test
    fun `when preferences fail before any success then products are rated with the default`() = runTest {
        val hummus = realProducts.named(HUMMUS)
        products.rows.value = Outcome.Success(listOf(hummus.product))
        preferences.current.value = Outcome.Failure(AppError.Storage)

        val analyses = analyzeProducts(setOf(hummus.barcode)).first()

        assertEquals(DietPreferences.Default, DietPreset.KETO.preferences)
        assertEquals(hummus.rates.getValue(DietPreset.KETO), rateOf(analyses, hummus.barcode.value))
        assertEquals(listOf(preferencesFailureReport), errorReporter.reports)
    }

    @Test
    fun `when preferences fail after a success then the last good profile keeps being used`() = runTest {
        val hummus = realProducts.named(HUMMUS)
        products.rows.value = Outcome.Success(listOf(hummus.product))
        preferences.current.value = Outcome.Success(DietPreset.LOW_CARB.preferences)
        val emissions = collect(setOf(hummus.barcode))

        preferences.current.value = Outcome.Failure(AppError.Storage)
        runCurrent()
        products.rows.value = Outcome.Success(listOf(hummus.product.copy(name = "Hummus 2")))
        runCurrent()

        assertEquals(listOf(GOOD, GOOD), emissions.map { rateOf(it, hummus.barcode.value) })
        assertEquals(listOf(preferencesFailureReport), errorReporter.reports)
    }

    private fun TestScope.collect(barcodes: Set<Barcode>): List<Map<Barcode, ProductAnalysis>> {
        val emissions = mutableListOf<Map<Barcode, ProductAnalysis>>()
        backgroundScope.launch { analyzeProducts(barcodes).toList(emissions) }
        runCurrent()
        return emissions
    }

    private fun rateOf(analyses: Map<Barcode, ProductAnalysis>, barcode: String): DietRate? =
        analyses[Barcode(barcode)]?.dietRate

    private class FakeProductRepository : ProductRepository {

        val rows = MutableStateFlow<Outcome<List<Product>>>(Outcome.Success(emptyList()))
        val observedBarcodes = mutableListOf<Set<Barcode>>()

        override fun observeProduct(barcode: String): Flow<Product> =
            rows.mapNotNull { outcome -> outcome.getOrNull()?.firstOrNull { it.barcode == barcode } }

        override fun observeProducts(barcodes: Set<Barcode>): Flow<List<Product>> {
            observedBarcodes += barcodes
            return rows.map { outcome ->
                outcome.getOrThrow().filter { Barcode(it.barcode) in barcodes }
            }
        }

        override suspend fun refreshProduct(barcode: String): Outcome<Unit> = throw NotImplementedError()
    }

    private class FakePreferencesRepository : DietPreferencesRepository {

        val current = MutableStateFlow<Outcome<DietPreferences>>(Outcome.Success(DietPreferences.Default))

        override fun observeDietPreferences(): Flow<DietPreferences> = current.map { it.getOrThrow() }

        override suspend fun getDietPreferences(): Outcome<DietPreferences> = throw NotImplementedError()
    }

    private companion object {
        const val MISSING_BARCODE = "missing"
        const val HUMMUS = "Hummus (Labeyrie)"
    }
}
