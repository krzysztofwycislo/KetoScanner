package app.carbpeek.feature.analysis.api.model

import app.carbpeek.feature.product.api.model.Product

sealed interface ProductAnalysis {

    val product: Product

    val ingredients: IngredientsAnalysis?

    val hasDiet: Boolean

    data class Rated(
        override val product: Product,
        val macronutrients: MacronutrientAnalysisResult,
        override val ingredients: IngredientsAnalysis? = null,
    ) : ProductAnalysis {
        override val hasDiet: Boolean get() = true
    }

    data class Unrated(
        override val product: Product,
        override val ingredients: IngredientsAnalysis? = null,
    ) : ProductAnalysis {
        override val hasDiet: Boolean get() = false
    }

    data class InsufficientData(
        override val product: Product,
        override val ingredients: IngredientsAnalysis? = null,
        override val hasDiet: Boolean = true,
    ) : ProductAnalysis
}

val ProductAnalysis.dietRate: DietRate?
    get() = (this as? ProductAnalysis.Rated)?.macronutrients?.rate

val ProductAnalysis.carbsPer100g: Double?
    get() = product.nutriments.carbohydratesPer100g
