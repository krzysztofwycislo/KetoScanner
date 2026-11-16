package app.carbpeek.feature.analysis.api.model

import app.carbpeek.feature.product.api.model.Product

sealed interface ProductAnalysis {

    val product: Product

    data class Rated(
        override val product: Product,
        val macronutrients: MacronutrientAnalysisResult,
    ) : ProductAnalysis

    data class InsufficientData(override val product: Product) : ProductAnalysis
}

val ProductAnalysis.dietRate: DietRate?
    get() = (this as? ProductAnalysis.Rated)?.macronutrients?.rate

val ProductAnalysis.carbsPer100g: Double?
    get() = product.nutriments.carbohydratesPer100g
