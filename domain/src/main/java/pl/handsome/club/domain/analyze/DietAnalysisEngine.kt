package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.product.Product


class DietAnalysisEngine {


    fun analyzeProduct(product: Product) : AnalyzedProduct {
        return AnalyzedProduct(product)
    }

}
