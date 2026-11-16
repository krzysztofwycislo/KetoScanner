package app.carbpeek.feature.analysis.api

import app.carbpeek.core.model.Barcode
import app.carbpeek.feature.analysis.api.model.ProductAnalysis
import kotlinx.coroutines.flow.Flow

interface AnalyzeProducts {
    operator fun invoke(barcodes: Set<Barcode>): Flow<Map<Barcode, ProductAnalysis>>
}
