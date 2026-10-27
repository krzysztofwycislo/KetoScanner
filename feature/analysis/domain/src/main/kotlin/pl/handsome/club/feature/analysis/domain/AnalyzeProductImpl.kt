package pl.handsome.club.feature.analysis.domain

import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.common.map
import pl.handsome.club.feature.analysis.api.AnalyzeProduct
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.product.api.ProductRepository

internal class AnalyzeProductImpl(
    private val products: ProductRepository,
    private val preferences: DietPreferencesRepository,
    private val engine: DietAnalysisEngine,
) : AnalyzeProduct {

    override suspend fun invoke(barcode: String): Outcome<ProductAnalysis> =
        products.getProduct(barcode).map { engine.analyze(preferences.getDietPreferences(), it) }
}
