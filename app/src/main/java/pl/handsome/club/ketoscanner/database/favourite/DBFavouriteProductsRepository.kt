package pl.handsome.club.ketoscanner.database.favourite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.handsome.club.domain.product.Product
import pl.handsome.club.domain.repository.FavouriteProductsRepository
import pl.handsome.club.ketoscanner.database.AppDatabase


class DBFavouriteProductsRepository(
    appDatabase: AppDatabase
) : FavouriteProductsRepository {

    private val favouriteProductsDao: FavouriteProductDao = appDatabase.favouriteProductsDao()

    override suspend fun addToFavourites(product: Product) {
        withContext(Dispatchers.IO) {
            favouriteProductsDao.insert(product.toFavouriteProductEntity())
        }
    }

}