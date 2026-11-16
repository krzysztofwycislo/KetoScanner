package app.carbpeek.feature.analysis.domain

import app.carbpeek.core.common.ErrorReporter
import app.carbpeek.core.model.Barcode
import app.carbpeek.feature.analysis.api.AnalyzeProducts
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import app.carbpeek.feature.preferences.api.DietPreferencesRepository
import app.carbpeek.feature.product.api.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class AnalyzeProductsImpl(
    private val products: ProductRepository,
    private val preferences: DietPreferencesRepository,
    private val engine: DietAnalysisEngine,
    private val errorReporter: ErrorReporter,
) : AnalyzeProducts {

    override fun invoke(barcodes: Set<Barcode>): Flow<Map<Barcode, ProductAnalysis>> =
        combine(
            products.observeProducts(barcodes),
            preferences.currentDietPreferences(errorReporter),
        ) { rows, diet ->
            rows.associate { product -> Barcode(product.barcode) to engine.analyze(diet, product) }
        }
}
