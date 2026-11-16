package app.carbpeek.feature.analysis.domain

import app.carbpeek.feature.analysis.api.AnalyzeProduct
import app.carbpeek.feature.analysis.api.AnalyzeProducts
import app.carbpeek.feature.analysis.api.DietRatingLimits
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val analysisDomainModule = module {
    factoryOf(::DietAnalysisEngine)
    factoryOf(::AnalyzeProductImpl) bind AnalyzeProduct::class
    factoryOf(::AnalyzeProductsImpl) bind AnalyzeProducts::class
    factoryOf(::DietRatingLimitsImpl) bind DietRatingLimits::class
}
