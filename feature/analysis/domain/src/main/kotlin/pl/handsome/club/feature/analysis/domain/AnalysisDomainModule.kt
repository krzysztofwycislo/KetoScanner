package pl.handsome.club.feature.analysis.domain

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val analysisDomainModule = module {
    factoryOf(::DietAnalysisEngine)
}
