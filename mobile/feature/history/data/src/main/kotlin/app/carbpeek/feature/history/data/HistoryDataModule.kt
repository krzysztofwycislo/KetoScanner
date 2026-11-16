package app.carbpeek.feature.history.data

import app.carbpeek.feature.history.api.AnalysisHistoryRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val historyDataModule = module {
    singleOf(::AnalysisHistoryPersistenceImpl) bind AnalysisHistoryPersistence::class
    singleOf(::AnalysisHistoryRepositoryImpl) bind AnalysisHistoryRepository::class
}
