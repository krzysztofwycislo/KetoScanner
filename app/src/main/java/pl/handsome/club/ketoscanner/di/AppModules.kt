package pl.handsome.club.ketoscanner.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import pl.handsome.club.domain.analyze.DietAnalysisEngine
import pl.handsome.club.domain.repository.AnalysisHistoryRepository
import pl.handsome.club.domain.repository.FavouriteProductsRepository
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.ketoscanner.database.AppDatabase
import pl.handsome.club.ketoscanner.database.favourite.DBFavouriteProductsRepository
import pl.handsome.club.ketoscanner.database.history.DBAnalysisHistoryRepository
import pl.handsome.club.ketoscanner.viewmodel.AnalysisHistoryViewModel
import pl.handsome.club.ketoscanner.viewmodel.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.FavouriteProductsViewModel
import pl.handsome.club.openfoodapi.OpenFoodFactsRepository


val appModules = module {
    listOf(
        // analysis
        single { DietAnalysisEngine() },

        // database
        single { AppDatabase.provide(androidContext()) },

        // repository
        single<ProductRepository> { OpenFoodFactsRepository() },
        single<pl.handsome.club.domain.repository.DietPreferencesRepository> { pl.handsome.club.ketoscanner.preferences.DietSharedPreferencesRepository() },
        single<AnalysisHistoryRepository> { DBAnalysisHistoryRepository(get()) },
        single<FavouriteProductsRepository> { DBFavouriteProductsRepository(get()) },

        // view model
        viewModel { AnalyzeProductViewModel(get(), get(), get(), get()) },
        viewModel { AnalysisHistoryViewModel(get()) },
        viewModel { FavouriteProductsViewModel(get()) }
    )
}
