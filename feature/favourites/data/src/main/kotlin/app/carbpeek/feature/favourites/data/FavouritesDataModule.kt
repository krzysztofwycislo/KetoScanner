package app.carbpeek.feature.favourites.data

import app.carbpeek.feature.favourites.api.FavouriteProductsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val favouritesDataModule = module {
    singleOf(::FavouritesPersistenceImpl) bind FavouritesPersistence::class
    singleOf(::FavouriteProductsRepositoryImpl) bind FavouriteProductsRepository::class
}
