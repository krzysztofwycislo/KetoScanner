package pl.handsome.club.domain.analyze

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import pl.handsome.club.domain.*
import pl.handsome.club.domain.preferences.DietPreferences


class DietAnalysisEngineTest {

    private val exampleProduct = createProduct(exampleNutrients)

    private val dietAnalysisEngine = DietAnalysisEngine()


    @Test
    fun `when analyze complete set of data then result should contain all types of results`() {
        val completePreferences =
            DietPreferences(KCAL_2500, exampleKetoPreferences, exampleIngredientPreferences)

        val analysisResult = dietAnalysisEngine.analyze(completePreferences, exampleProduct)

        assertNotNull(analysisResult)
        assertEquals(exampleProduct, analysisResult.product)
    }

}