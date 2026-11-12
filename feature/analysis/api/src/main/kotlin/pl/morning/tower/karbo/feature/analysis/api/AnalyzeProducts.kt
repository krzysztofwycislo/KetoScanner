package pl.morning.tower.karbo.feature.analysis.api

import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.model.Barcode
import pl.morning.tower.karbo.feature.analysis.api.model.ProductAnalysis

interface AnalyzeProducts {
    operator fun invoke(barcodes: Set<Barcode>): Flow<Map<Barcode, ProductAnalysis>>
}
