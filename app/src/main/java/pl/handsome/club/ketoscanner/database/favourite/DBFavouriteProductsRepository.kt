package pl.handsome.club.ketoscanner.database.favourite

import androidx.paging.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.handsome.club.domain.product.FavouriteProduct
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

    override suspend fun load(): DataSource.Factory<Int, FavouriteProduct> {
        return favouriteProductsDao
            .loadFavouriteProductsOrderByUpdateTime()
            .map { it.toDomain() }
    }

    override suspend fun findByBarcode(barcode: String): FavouriteProduct? {
        return withContext(Dispatchers.IO) {
            favouriteProductsDao.findByBarcode(barcode)
        }
    }


}