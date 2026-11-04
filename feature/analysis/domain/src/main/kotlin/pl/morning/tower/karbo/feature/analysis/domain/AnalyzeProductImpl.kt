package pl.handsome.club.feature.analysis.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.handsome.club.core.common.getOrNull
import pl.handsome.club.feature.analysis.api.AnalyzeProduct
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis
import pl.handsome.club.feature.preferences.api.DietPreferencesRepository
import pl.handsome.club.feature.preferences.api.model.DietPreferences
import pl.handsome.club.feature.product.api.ProductRepository

internal class AnalyzeProductImpl(
    private val products: ProductRepository,
    private val preferences: DietPreferencesRepository,
    private val engine: DietAnalysisEngine,
) : AnalyzeProduct {

    override fun invoke(barcode: String): Flow<ProductAnalysis> =
        products.observeProduct(barcode).map { product ->
            engine.analyze(preferences.getDietPreferences().getOrNull() ?: DietPreferences.Default, product)
        }
}
