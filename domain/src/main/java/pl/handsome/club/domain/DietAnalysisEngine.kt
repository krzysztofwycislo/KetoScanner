package pl.handsome.club.domain

import pl.handsome.club.domain.data.AnalyzedProduct
import pl.handsome.club.domain.data.Product


class DietAnalysisEngine {


    fun analyzeProduct(product: Product) : AnalyzedProduct {
        return AnalyzedProduct(product)
    }

}
