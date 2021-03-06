package pl.handsome.club.domain.analyze

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import pl.handsome.club.domain.analyze.macronutrient.DietRate
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalyzer
import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.product.Product


@RunWith(Parameterized::class)
class MacronutrientAnalyzerTest(
    private val product: Product,
    private val preferences: DietPreferences,
    private val carbsRate: DietRate
) {

    private val analyzer = MacronutrientAnalyzer

    @Test
    fun `when we want to analyze product then result we should get correct ratings`() {
        val result = analyzer.analyze(preferences, product.nutriments)

        assertThat(result).isNotNull()
        if (result != null) {
            assertThat(result.carbsRate).isEqualTo(carbsRate)
        }
    }


    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} fat = {2} carbs = {3}")
        fun data() = macronutrientAnalyzerTestParameters
    }

}