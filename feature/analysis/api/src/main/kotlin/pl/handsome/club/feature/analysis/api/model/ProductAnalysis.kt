package pl.handsome.club.feature.analysis.api.model

import pl.handsome.club.feature.product.api.model.Product

sealed interface ProductAnalysis {

    val product: Product

    data class Rated(
        override val product: Product,
        val macronutrients: MacronutrientAnalysisResult,
    ) : ProductAnalysis

    data class InsufficientData(override val product: Product) : ProductAnalysis
}
