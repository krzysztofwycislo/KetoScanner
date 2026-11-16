package app.carbpeek.feature.analysis.api

import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import kotlinx.coroutines.flow.Flow

interface AnalyzeProduct {
    operator fun invoke(barcode: String): Flow<ProductAnalysis>
}
