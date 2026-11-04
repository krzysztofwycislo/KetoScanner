package pl.morning.tower.karbo.feature.analysis.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.morning.tower.karbo.core.common.getOrNull
import pl.morning.tower.karbo.feature.analysis.api.AnalyzeProduct
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.preferences.api.model.DietPreferences
import pl.morning.tower.karbo.feature.product.api.ProductRepository

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
