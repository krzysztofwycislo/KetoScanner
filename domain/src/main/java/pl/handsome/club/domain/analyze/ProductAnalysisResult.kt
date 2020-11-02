package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.product.Product


data class ProductAnalysisResult(
    val product: Product,
    val generalDietAnalysisResult: GeneralAnalysisResult,
    val ingredientAnalysisResult: IngredientAnalysisResult?,
    val macronutrientAnalysisResult: MacronutrientAnalysisResult?
)
