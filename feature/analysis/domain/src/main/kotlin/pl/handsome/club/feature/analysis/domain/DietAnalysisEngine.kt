package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.model.Product


internal class DietAnalysisEngine {

    fun analyze(preferences: DietPreferences, product: Product): ProductAnalysis {
        val macronutrients = MacronutrientAnalyzer.analyze(preferences, product.nutriments)
            ?: return ProductAnalysis.InsufficientData(product)

        return ProductAnalysis.Rated(product, macronutrients)
    }

}
