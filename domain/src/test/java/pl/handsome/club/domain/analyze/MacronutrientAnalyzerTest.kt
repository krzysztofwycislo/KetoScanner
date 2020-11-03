package pl.handsome.club.domain.analyze

import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalyzer
import pl.handsome.club.domain.analyze.macronutrient.KetoRate
import pl.handsome.club.domain.preferences.MacronutrientPreferences
import pl.handsome.club.domain.product.Product

@RunWith(Parameterized::class)
class MacronutrientAnalyzerTest(
    private val product: Product,
    private val preferences: MacronutrientPreferences,
    private val fatRate: KetoRate,
    private val carbsRate: KetoRate
) {

    private val analyzer = MacronutrientAnalyzer

    @Test
    fun `when we want to analyze product then result we should return set of correct ratings`() {
        val result = analyzer.analyze(preferences, product.nutriments)

        assertTrue(result is MacronutrientAnalysisResult.Success)
        result as MacronutrientAnalysisResult.Success

        assertTrue(result.fatRate == fatRate)
        assertTrue(result.carbsRate == carbsRate)
    }


    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} fat = {2} carbs = {3}")
        fun data() = macronutrientAnalyzerTestParameters
    }

}