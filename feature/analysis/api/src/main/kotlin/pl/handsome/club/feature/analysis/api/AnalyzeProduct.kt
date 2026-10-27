package pl.handsome.club.feature.analysis.api

import pl.handsome.club.core.common.Outcome
import pl.handsome.club.feature.analysis.api.model.ProductAnalysis

interface AnalyzeProduct {
    suspend operator fun invoke(barcode: String): Outcome<ProductAnalysis>
}
