package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.preferences.IngredientPreferences
import pl.handsome.club.domain.product.Product

internal object IngredientAnalyser {

    fun analyze(ingredientPreferences: IngredientPreferences, product: Product): IngredientAnalysisResult {
        return IngredientAnalysisResult.Success()
    }

}
