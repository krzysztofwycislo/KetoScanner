package pl.handsome.club.core.database

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.provide(androidContext()) }
    single { get<AppDatabase>().productAnalysisHistoryDao() }
    single { get<AppDatabase>().productDao() }
    single { get<AppDatabase>().favouriteProductsDao() }
}
