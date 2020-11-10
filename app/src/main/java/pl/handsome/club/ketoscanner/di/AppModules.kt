package pl.handsome.club.ketoscanner.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.handsome.club.domain.analyze.DietAnalysisEngine
import pl.handsome.club.domain.repository.AnalysisHistoryRepository
import pl.handsome.club.domain.repository.DietPreferencesRepository
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.ketoscanner.repository.DBAnalysisHistoryRepository
import pl.handsome.club.ketoscanner.repository.DBDietPreferencesRepository
import pl.handsome.club.ketoscanner.repository.database.AppDatabase
import pl.handsome.club.ketoscanner.viewmodel.AnalysisHistoryViewModel
import pl.handsome.club.ketoscanner.viewmodel.AnalyzeProductViewModel
import pl.handsome.club.openfoodapi.OpenFoodFactsRepository


val appModules = module {
    listOf(
        // analysis
        single { DietAnalysisEngine() },

        // database
        single { AppDatabase.provide(androidContext()) },

        // repository
        single<ProductRepository> { OpenFoodFactsRepository() },
        single<DietPreferencesRepository> { DBDietPreferencesRepository() },
        single<AnalysisHistoryRepository> { DBAnalysisHistoryRepository(get()) },

        // view model
        viewModel { AnalyzeProductViewModel(get(), get(), get(), get()) },
        viewModel { AnalysisHistoryViewModel(get()) }
    )
}