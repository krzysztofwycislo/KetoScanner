package pl.handsome.club.feature.favourites.data

import kotlinx.coroutines.flow.Flow
import pl.handsome.club.core.database.favourite.FavouriteProductDao
import pl.handsome.club.core.database.favourite.FavouriteProductEntity

internal interface FavouritesPersistence {
    fun observe(nameQuery: String): Flow<List<FavouriteProductEntity>>
    suspend fun insert(entity: FavouriteProductEntity)
    suspend fun delete(entity: FavouriteProductEntity)
    suspend fun findByBarcode(barcode: String): FavouriteProductEntity?
}

internal class FavouritesPersistenceImpl(
    private val dao: FavouriteProductDao,
) : FavouritesPersistence {

    override fun observe(nameQuery: String): Flow<List<FavouriteProductEntity>> =
        dao.searchAndOrderByUpdateTime(nameQuery)

    override suspend fun insert(entity: FavouriteProductEntity) = dao.insert(entity)

    override suspend fun delete(entity: FavouriteProductEntity) = dao.delete(entity)

    override suspend fun findByBarcode(barcode: String): FavouriteProductEntity? = dao.findByBarcode(barcode)
}
