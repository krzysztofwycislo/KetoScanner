package app.carbpeek.feature.favourites.data

import app.carbpeek.core.database.favourite.FavouriteProductDao
import app.carbpeek.core.database.favourite.FavouriteProductEntity
import kotlinx.coroutines.flow.Flow

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
