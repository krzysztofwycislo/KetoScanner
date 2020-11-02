package pl.handsome.club.domain.analyze

import pl.handsome.club.domain.product.Product

internal object GeneralAnalyser {

    fun analyze(kcalPerDay: Int, product: Product): GeneralAnalysisResult {
        return GeneralAnalysisResult.Success(false)
    }

}
