package pl.handsome.club.domain.analyze

import org.junit.Assert.*
import org.junit.Test
import pl.handsome.club.domain.*
import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult

import pl.handsome.club.domain.preferences.DietPreferences


class DietAnalysisEngineTest {

    private val exampleProduct = createProduct(exampleNutrients)

    private val dietAnalysisEngine = DietAnalysisEngine()


    @Test
    fun `when analyze complete set of data then result should contain all types of results`() {
        val completePreferences =
            DietPreferences(KCAL_2500, exampleIngredientPreferences, exampleKetoPreferences)

        val analysisResult = dietAnalysisEngine.analyze(completePreferences, exampleProduct)

        with(analysisResult) {
            assertEquals(exampleProduct, product)
            assertTrue(ingredientAnalysisResult is IngredientAnalysisResult.Success)
            assertTrue(macronutrientAnalysisResult is MacronutrientAnalysisResult.Success)
        }
    }

    @Test
    fun `when analyze data without ingredient preferences preferences then information about it should be in result`() {
        val preferences = DietPreferences(KCAL_2500, null, exampleKetoPreferences)

        val analysisResult = dietAnalysisEngine.analyze(preferences, exampleProduct)

        with(analysisResult) {
            assertEquals(exampleProduct, product)
            assertTrue(ingredientAnalysisResult is IngredientAnalysisResult.NoPreferences)
            assertTrue(macronutrientAnalysisResult is MacronutrientAnalysisResult.Success)
        }
    }

    @Test
    fun `when analyze data without macronutrient preferences then information about it should be in result`() {
        val preferences = DietPreferences(KCAL_2500, exampleIngredientPreferences, null)

        val analysisResult = dietAnalysisEngine.analyze(preferences, exampleProduct)

        with(analysisResult) {
            assertEquals(exampleProduct, product)
            assertTrue(ingredientAnalysisResult is IngredientAnalysisResult.Success)
            assertTrue(macronutrientAnalysisResult is MacronutrientAnalysisResult.NoPreferences)
        }
    }

}