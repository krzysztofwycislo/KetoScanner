package pl.handsome.club.domain.analyze.ingredient

import pl.handsome.club.domain.preferences.IngredientPreferences
import pl.handsome.club.domain.product.Product

internal object IngredientAnalyzer {

    fun analyze(ingredientPreferences: IngredientPreferences, product: Product): IngredientAnalysisResult {
        return IngredientAnalysisResult()
    }

}
