package pl.handsome.club.feature.analysis.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class DietAnalysisEngineTest {

    private val exampleProduct = createProduct(exampleNutrients)

    private val dietAnalysisEngine = DietAnalysisEngine()


    @Test
    fun `when analyze complete set of data then result should contain all types of results`() {
        val completePreferences = exampleDietPreferences

        val analysisResult = dietAnalysisEngine.analyze(completePreferences, exampleProduct)

        assertEquals(exampleProduct, analysisResult.product)
        assertNotNull(analysisResult.macronutrientAnalysisResult)
    }

}
