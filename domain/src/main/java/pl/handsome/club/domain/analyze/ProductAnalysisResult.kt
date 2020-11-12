package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.analyze.ingredient.IngredientAnalysisResult
import pl.handsome.club.domain.analyze.macronutrient.MacronutrientAnalysisResult
import pl.handsome.club.domain.product.Product


data class ProductAnalysisResult(
    val product: Product,
    val macronutrientAnalysisResult: MacronutrientAnalysisResult,
    val ingredientAnalysisResult: IngredientAnalysisResult
)
