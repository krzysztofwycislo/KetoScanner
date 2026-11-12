package pl.morning.tower.karbo.feature.analysis.domain

import pl.morning.tower.karbo.core.model.ProductCategory
import pl.morning.tower.karbo.feature.analysis.api.model.DietRate
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.preferences.api.model.DietMode
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull


class DietAnalysisEngineTest {

    private val exampleProduct = createProduct(exampleNutrients)

    private val dietAnalysisEngine = DietAnalysisEngine()


    @Test
    fun `when analyze complete set of data then result is rated`() {
        val completePreferences = exampleDietPreferences

        val analysisResult = dietAnalysisEngine.analyze(completePreferences, exampleProduct)

        val rated = assertIs<ProductAnalysis.Rated>(analysisResult)
        assertEquals(exampleProduct, rated.product)
    }

    @Test
    fun `when product has no carbs fat or protein per 100g then result is insufficient data`() {
        listOf(
            exampleNutrients.copy(carbohydratesPer100g = null),
            exampleNutrients.copy(fatsPer100g = null),
            exampleNutrients.copy(proteinsPer100g = null),
        ).forEach { nutriments ->
            val product = createProduct(nutriments)

            val analysisResult = dietAnalysisEngine.analyze(exampleDietPreferences, product)

            assertEquals(ProductAnalysis.InsufficientData(product), analysisResult)
        }
    }

    @Test
    fun `when sugars and serving are unknown then the product is still rated`() {
        val product = createProduct("plain", macroNutriments(20.0, 10.0, 2.0, sugarsPer100g = null), null)

        val rated = assertIs<ProductAnalysis.Rated>(dietAnalysisEngine.analyze(exampleDietPreferences, product))
        assertEquals(DietRate.GOOD, rated.macronutrients.rate)
    }

    @Test
    fun `result carries the mode daily limit and carbs per 100 g`() {
        val result = rated(DietPreferences(30, DietMode.LOW_CARB), exampleProduct)

        assertEquals(DietMode.LOW_CARB, result.dietMode)
        assertEquals(30, result.dailyCarbConsumption)
        assertEquals(10.0, result.carbsPer100g)
    }

    @Test
    fun `serving is parsed from trimmed positive numbers only`() {
        val nutriments = macroNutriments(50.0, 0.0, 10.0)

        assertEquals(30.0, createProduct("p", nutriments, " 30 ").toNutritionProfile()?.servingGrams)
        assertEquals(
            500.0,
            createProduct(name = "p", productNutriments = nutriments, servingAmount = "500")
                .toNutritionProfile()
                ?.servingGrams,
        )
        listOf(null, "", " ", "abc", "0", "-5", "501", "1000").forEach { serving ->
            assertNull(
                createProduct("p", nutriments, serving).toNutritionProfile()?.servingGrams,
                "serving \"$serving\"",
            )
        }
    }

    @Test
    fun `the profile carries fibre and marks drinks, juices and water as liquid`() {
        val product = createProduct(
            name = "p",
            productNutriments = macroNutriments(
                fatsPer100g = 0.1,
                proteinsPer100g = 1.0,
                carbohydratesPer100g = 9.0,
                fibrePer100g = 2.0,
            ),
            servingAmount = null,
        )

        assertEquals(2.0, product.toNutritionProfile()?.fibre)
        listOf(ProductCategory.DRINKS, ProductCategory.MILK_AND_JUICE, ProductCategory.WATER).forEach { category ->
            assertEquals(true, product.copy(category = category).toNutritionProfile()?.liquid, "$category")
        }
        listOf(ProductCategory.OTHER, ProductCategory.TEA_AND_GREENS).forEach { category ->
            assertEquals(false, product.copy(category = category).toNutritionProfile()?.liquid, "$category")
        }
    }

    private fun rated(preferences: DietPreferences, product: pl.morning.tower.karbo.feature.product.api.model.Product) =
        assertIs<ProductAnalysis.Rated>(dietAnalysisEngine.analyze(preferences, product)).macronutrients

}
