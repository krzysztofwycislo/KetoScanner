package pl.morning.tower.karbo.feature.analysis.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pl.morning.tower.karbo.core.common.ErrorReporter
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.analysis.api.AnalyzeProducts
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis
import pl.morning.tower.karbo.feature.preferences.api.DietPreferencesRepository
import pl.morning.tower.karbo.feature.product.api.ProductRepository

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
