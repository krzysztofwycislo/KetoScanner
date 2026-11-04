package pl.handsome.club.feature.analysis.api

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis

interface AnalyzeProduct {
    operator fun invoke(barcode: String): Flow<ProductAnalysis>
}
