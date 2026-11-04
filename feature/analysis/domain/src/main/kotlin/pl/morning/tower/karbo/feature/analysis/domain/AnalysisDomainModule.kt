package pl.morning.tower.karbo.feature.analysis.domain

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.analysis.api.AnalyzeProduct
import pl.morning.tower.karbo.feature.analysis.api.DietRatingLimits

val analysisDomainModule = module {
    factoryOf(::DietAnalysisEngine)
    factoryOf(::AnalyzeProductImpl) bind AnalyzeProduct::class
    factoryOf(::DietRatingLimitsImpl) bind DietRatingLimits::class
}
