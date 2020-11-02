package pl.handsome.club.domain.analyze.general

import pl.handsome.club.domain.product.Product

internal object GeneralAnalyzer {

    fun analyze(kcalPerDay: Int, product: Product): GeneralAnalysisResult {
        return GeneralAnalysisResult.Success(false)
    }

}
