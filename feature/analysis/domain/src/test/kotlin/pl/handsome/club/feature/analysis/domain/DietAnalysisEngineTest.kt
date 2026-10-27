package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


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
    fun `when product has no carbs per 100g then result is insufficient data`() {
        val product = createProduct(exampleNutrients.copy(carbohydratesPer100g = null))

        val analysisResult = dietAnalysisEngine.analyze(exampleDietPreferences, product)

        assertEquals(ProductAnalysis.InsufficientData(product), analysisResult)
    }

}
