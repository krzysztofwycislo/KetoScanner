package pl.morning.tower.karbo.feature.analysis.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pl.morning.tower.karbo.core.common.ErrorReporter
import pl.morning.tower.karbo.feature.analysis.api.AnalyzeProduct
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.product.api.ProductRepository

internal class AnalyzeProductImpl(
    private val products: ProductRepository,
    private val preferences: DietPreferencesRepository,
    private val engine: DietAnalysisEngine,
    private val errorReporter: ErrorReporter,
) : AnalyzeProduct {

    override fun invoke(barcode: String): Flow<ProductAnalysis> =
        combine(
            products.observeProduct(barcode),
            preferences.currentDietPreferences(errorReporter),
        ) { product, diet ->
            engine.analyze(diet, product)
        }
}
