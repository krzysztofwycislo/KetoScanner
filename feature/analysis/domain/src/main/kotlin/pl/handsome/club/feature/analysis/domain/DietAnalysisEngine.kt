package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.feature.analysis.api.model.ProductAnalysisResult
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.model.Product


open class DietAnalysisEngine {

    open fun analyze(preferences: DietPreferences, product: Product): ProductAnalysisResult {
        val macronutrientAnalysisResult = MacronutrientAnalyzer.analyze(
            preferences,
            product.nutriments
        )

        return ProductAnalysisResult(
            product,
            macronutrientAnalysisResult
        )
    }

}
