package pl.handsome.club.ketoscanner.database.favourite

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.handsome.club.core.common.AppDispatchers
import pl.handsome.club.core.common.AppError
import pl.handsome.club.core.common.Outcome
import pl.handsome.club.core.common.outcomeOf
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.favourites.api.FavouriteProductsRepository
import pl.handsome.club.core.database.AppDatabase
import pl.handsome.club.core.database.favourite.FavouriteProductDao
import pl.handsome.club.core.database.favourite.FavouriteProductEntity


class DBFavouriteProductsRepository(
    appDatabase: AppDatabase,
    private val dispatchers: AppDispatchers
) : FavouriteProductsRepository {

    private val favouriteProductsDao: FavouriteProductDao = appDatabase.favouriteProductsDao()


    override suspend fun addToFavourites(product: Product): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                favouriteProductsDao.insert(product.toFavouriteProductEntity())
            }
        }

    override suspend fun search(name: String): Flow<List<FavouriteProduct>> =
        favouriteProductsDao
            .searchAndOrderByUpdateTime(name)
            .map { list -> list.map(FavouriteProductEntity::toDomain) }
            .flowOn(dispatchers.io)
            .conflate()


    override suspend fun findByBarcode(barcode: String): Outcome<FavouriteProduct?> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                favouriteProductsDao.findByBarcode(barcode)?.toDomain()
            }
        }

    override suspend fun removeFromFavourites(product: Product): Outcome<Unit> =
        withContext(dispatchers.io) {
            outcomeOf(mapError = { AppError.Storage }) {
                favouriteProductsDao.delete(product.toFavouriteProductEntity())
            }
        }

}
