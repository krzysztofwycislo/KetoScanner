package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.preferences.DietPreferences
import pl.handsome.club.domain.product.Product


class DietAnalysisEngine {

    fun analyzeProduct(preferences: DietPreferences, product: Product) : AnalyzedProduct {
        return AnalyzedProduct(product)
    }

}
