package pl.morning.tower.karbo.feature.product.data

import kotlinx.coroutines.flow.Flow
import pl.morning.tower.karbo.core.database.product.ProductDao
import pl.morning.tower.karbo.core.database.product.ProductEntity

internal interface ProductPersistence {
    suspend fun find(barcode: String): ProductEntity?
    fun observe(barcode: String): Flow<ProductEntity?>
    suspend fun save(product: ProductEntity)
    suspend fun keepNewest(amount: Int)
}

internal class ProductPersistenceImpl(
    private val dao: ProductDao,
) : ProductPersistence {

    override suspend fun find(barcode: String): ProductEntity? = dao.findByBarcode(barcode)

    override fun observe(barcode: String): Flow<ProductEntity?> = dao.observeByBarcode(barcode)

    override suspend fun save(product: ProductEntity) = dao.upsert(product)

    override suspend fun keepNewest(amount: Int) = dao.deleteAllExceptNewest(amount)
}
