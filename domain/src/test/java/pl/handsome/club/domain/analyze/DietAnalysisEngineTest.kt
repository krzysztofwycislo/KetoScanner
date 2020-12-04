package pl.handsome.club.domain.analyze

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import pl.handsome.club.domain.createProduct
import pl.handsome.club.domain.exampleDietPreferences
import pl.handsome.club.domain.exampleNutrients


class DietAnalysisEngineTest {

    private val exampleProduct = createProduct(exampleNutrients)

    private val dietAnalysisEngine = DietAnalysisEngine()


    @Test
    fun `when analyze complete set of data then result should contain all types of results`() {
        val completePreferences = exampleDietPreferences

        val analysisResult = dietAnalysisEngine.analyze(completePreferences, exampleProduct)

        assertNotNull(analysisResult)
        assertEquals(exampleProduct, analysisResult.product)
    }

}