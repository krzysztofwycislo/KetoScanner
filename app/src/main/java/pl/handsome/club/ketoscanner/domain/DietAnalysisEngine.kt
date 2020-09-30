package pl.handsome.club.ketoscanner.domain


class DietAnalysisEngine {


    fun analyzeProduct(product: Product) : AnalysisResult {
        return AnalysisResult(product)
    }

}
