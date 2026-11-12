package pl.morning.tower.karbo.feature.analysis.domain

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
import pl.morning.tower.karbo.core.common.AppError
import pl.morning.tower.karbo.core.common.AppErrorException
import pl.morning.tower.karbo.core.common.Outcome
import pl.morning.tower.karbo.core.common.getOrNull
import pl.morning.tower.karbo.core.common.getOrThrow
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.core.testing.FakeErrorReporter
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate.GOOD
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate.NEUTRAL
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate.NOT_ADVISED
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.analysis.api.model.dietRate
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreset
import pl.morning.tower.karbo.feature.product.api.ProductRepository
import pl.morning.tower.karbo.feature.product.api.model.Product
import pl.morning.tower.karbo.feature.product.api.model.ProductNutriments
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
        products.rows.value = Outcome.Success(foods.map { it.product })
        val emissions = collect(foods.map { it.barcode }.toSet())

        DietPreset.entries.forEach { preset ->
            preferences.current.value = Outcome.Success(preset.preferences)
            runCurrent()

            val analyses = emissions.last()
            foods.forEach { food ->
                assertEquals(food.rates.getValue(preset), analyses[food.barcode]?.dietRate, "${food.name} in $preset")
            }
        }
        assertEquals(1, products.observedBarcodes.size)
    }

    @Test
    fun `when a list and a single analysis rate the same product then they agree`() = runTest {
        products.rows.value = Outcome.Success(foods.map { it.product })
        val analyzeProduct = AnalyzeProductImpl(products, preferences, engine, errorReporter)

        DietPreset.entries.forEach { preset ->
            preferences.current.value = Outcome.Success(preset.preferences)
            val analyses = analyzeProducts(foods.map { it.barcode }.toSet()).first()

            foods.forEach { food ->
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
        val hummus = foods.first().product
        products.rows.value = Outcome.Success(listOf(hummus))

        val analyses = analyzeProducts(setOf(Barcode(hummus.barcode), Barcode(MISSING_BARCODE))).first()

        assertEquals(
            setOf(Barcode(hummus.barcode)),
            analyses.keys,
        )
    }

    @Test
    fun `when a product emission changes the nutrition then the rate is updated`() = runTest {
        val hummus = foods.first().product
        val bread = foods.single { it.name == "White bread" }.product.copy(barcode = hummus.barcode)
        products.rows.value = Outcome.Success(listOf(hummus))
        val emissions = collect(setOf(Barcode(hummus.barcode)))

        products.rows.value = Outcome.Success(listOf(bread))
        runCurrent()

        assertEquals(listOf(NEUTRAL, NOT_ADVISED), emissions.map { rateOf(it, hummus.barcode) })
    }

    @Test
    fun `when products fail then the same error is thrown`() = runTest {
        products.rows.value = Outcome.Failure(AppError.Storage)

        val exception = assertFailsWith<AppErrorException> { analyzeProducts(emptySet()).first() }

        assertEquals(AppError.Storage, exception.error)
    }

    @Test
    fun `when preferences fail before any success then products are rated with the default`() = runTest {
        val hummus = foods.first()
        products.rows.value = Outcome.Success(listOf(hummus.product))
        preferences.current.value = Outcome.Failure(AppError.Storage)

        val analyses = analyzeProducts(setOf(hummus.barcode)).first()

        assertEquals(DietPreferences.Default, DietPreset.KETO.preferences)
        assertEquals(hummus.rates.getValue(DietPreset.KETO), rateOf(analyses, hummus.barcode.value))
        assertEquals(listOf(preferencesFailureReport), errorReporter.reports)
    }

    @Test
    fun `when preferences fail after a success then the last good profile keeps being used`() = runTest {
        val hummus = foods.first()
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

    private class Food(val product: Product, val rates: Map<DietPreset, DietRate>) {
        val name: String get() = product.name
        val barcode: Barcode get() = Barcode(product.barcode)
    }

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

        override suspend fun setDietPreferences(preferences: DietPreferences): Outcome<Unit> =
            throw NotImplementedError()
    }

    private companion object {
        const val MISSING_BARCODE = "missing"

        fun food(name: String, nutriments: ProductNutriments, serving: String?): Product =
            createProduct(name, nutriments, serving).copy(barcode = "barcode $name")

        fun rates(strictKeto: DietRate, keto: DietRate, lowCarb: DietRate) = mapOf(
            DietPreset.STRICT_KETO to strictKeto,
            DietPreset.KETO to keto,
            DietPreset.LOW_CARB to lowCarb,
        )

        val foods = listOf(
            Food(
                food("Hummus", macroNutriments(20.0, 7.0, 12.0, 0.5), null),
                rates(NOT_ADVISED, NEUTRAL, GOOD),
            ),
            Food(
                food("Greek yoghurt 10 %", macroNutriments(10.0, 4.0, 3.5, 3.5), "150"),
                rates(NEUTRAL, GOOD, GOOD),
            ),
            Food(
                food("Milk 3.2 %", macroNutriments(3.2, 3.3, 4.8, 4.8), "250"),
                rates(NOT_ADVISED, NOT_ADVISED, NEUTRAL),
            ),
            Food(
                food("White bread", macroNutriments(3.0, 8.0, 49.0, 5.0), null),
                rates(NOT_ADVISED, NOT_ADVISED, NOT_ADVISED),
            ),
            Food(
                food("Olive oil", macroNutriments(91.6, 0.0, 0.0, 0.0), null),
                rates(GOOD, GOOD, GOOD),
            ),
        )
    }
}
