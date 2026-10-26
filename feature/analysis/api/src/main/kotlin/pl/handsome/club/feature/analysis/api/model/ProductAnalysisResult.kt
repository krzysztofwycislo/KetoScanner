package pl.handsome.club.feature.analysis.api.model

import pl.handsome.club.feature.product.api.model.Product


data class ProductAnalysisResult(
    val product: Product,
    val macronutrientAnalysisResult: MacronutrientAnalysisResult?
)
