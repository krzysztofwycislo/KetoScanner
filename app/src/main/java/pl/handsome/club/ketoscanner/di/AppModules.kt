package pl.handsome.club.ketoscanner.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.handsome.club.domain.analyze.DietAnalysisEngine
import pl.handsome.club.domain.repository.AnalysisHistoryRepository
import pl.handsome.club.domain.repository.DietPreferencesRepository
import pl.handsome.club.domain.repository.FavouriteProductsRepository
import pl.handsome.club.domain.repository.ProductRepository
import pl.handsome.club.ketoscanner.database.AppDatabase
import pl.handsome.club.ketoscanner.database.favourite.DBFavouriteProductsRepository
import pl.handsome.club.ketoscanner.database.history.DBAnalysisHistoryRepository
import pl.handsome.club.ketoscanner.preferences.DataStoreDietPreferencesRepository
import pl.handsome.club.ketoscanner.preferences.createDietPreferencesDataStore
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalysisHistoryViewModel
import pl.handsome.club.ketoscanner.viewmodel.analyze.AnalyzeProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.favourite.add.AddFavouriteProductViewModel
import pl.handsome.club.ketoscanner.viewmodel.favourite.list.FavouriteProductsListViewModel
import pl.handsome.club.ketoscanner.viewmodel.preferences.DietPreferencesViewModel
import pl.handsome.club.ketoscanner.BuildConfig
import pl.handsome.club.openfoodapi.OpenFoodFactsRepository
import pl.handsome.club.openfoodapi.api.OPEN_FOOD_FACTS_BASE_URL
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsApi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsMoshi
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsOkHttpClient
import pl.handsome.club.openfoodapi.api.createOpenFoodFactsRetrofit


private const val OPEN_FOOD_FACTS_CONTACT = "https://github.com/krzysztofwycislo/KetoScanner"

val appModules = module {
    listOf(
        // analysis
        single { DietAnalysisEngine() },

        // database
        single { AppDatabase.provide(androidContext()) },

        // network
        single { createOpenFoodFactsMoshi() },
        single {
            createOpenFoodFactsOkHttpClient(
                userAgent = "KetoScanner/${BuildConfig.VERSION_NAME} ($OPEN_FOOD_FACTS_CONTACT)",
                enableBodyLogging = BuildConfig.DEBUG
            )
        },
        single { createOpenFoodFactsRetrofit(OPEN_FOOD_FACTS_BASE_URL, get(), get()) },
        single { createOpenFoodFactsApi(get()) },

        // repository
        single<ProductRepository> { OpenFoodFactsRepository(get()) },
        single<DietPreferencesRepository> { DataStoreDietPreferencesRepository(get()) },
        single<AnalysisHistoryRepository> { DBAnalysisHistoryRepository(get()) },
        single<FavouriteProductsRepository> { DBFavouriteProductsRepository(get()) },

        // preferences
        single<DataStore<Preferences>> { createDietPreferencesDataStore(androidContext()) },

        // view model
        viewModel { AnalyzeProductViewModel(get(), get(), get(), get()) },
        viewModel { AnalysisHistoryViewModel(get()) },
        viewModel { AddFavouriteProductViewModel(get()) },
        viewModel { FavouriteProductsListViewModel(get()) },
        viewModel { DietPreferencesViewModel(get()) }
    )
}
