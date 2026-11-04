package pl.morning.tower.karbo.feature.favourites.data

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pl.morning.tower.karbo.feature.favourites.api.FavouriteProductsRepository

val favouritesDataModule = module {
    singleOf(::FavouritesPersistenceImpl) bind FavouritesPersistence::class
    singleOf(::FavouriteProductsRepositoryImpl) bind FavouriteProductsRepository::class
}
