package pl.morning.tower.karbo.feature.analysis.api

import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis

interface AnalyzeProduct {
    operator fun invoke(barcode: String): Flow<ProductAnalysis>
}
