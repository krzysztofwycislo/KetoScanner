package pl.handsome.club.ketoscanner.database.favourite

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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

    override suspend fun search(name: String): Flow<List<FavouriteProduct>> =
        favouriteProductsDao
            .searchAndOrderByUpdateTime(name)
            .map { list -> list.map(FavouriteProductEntity::toDomain) }
            .flowOn(Dispatchers.IO)
            .conflate()


    override suspend fun findByBarcode(barcode: String): FavouriteProduct? {
        return withContext(Dispatchers.IO) {
            favouriteProductsDao.findByBarcode(barcode)
        }
    }

    override suspend fun removeFromFavourites(product: Product) {
        return withContext(Dispatchers.IO) {
            favouriteProductsDao.delete(product.toFavouriteProductEntity())
        }
    }

}