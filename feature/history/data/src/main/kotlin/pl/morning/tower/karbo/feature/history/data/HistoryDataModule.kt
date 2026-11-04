package pl.morning.tower.karbo.feature.history.data

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.history.api.AnalysisHistoryRepository

val historyDataModule = module {
    singleOf(::AnalysisHistoryPersistenceImpl) bind AnalysisHistoryPersistence::class
    singleOf(::AnalysisHistoryRepositoryImpl) bind AnalysisHistoryRepository::class
}
