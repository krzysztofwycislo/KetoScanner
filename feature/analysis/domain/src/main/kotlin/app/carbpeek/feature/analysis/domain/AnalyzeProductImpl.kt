package app.carbpeek.feature.analysis.domain

import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.feature.analysis.api.AnalyzeProduct
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.product.api.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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
