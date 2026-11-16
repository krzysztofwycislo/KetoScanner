package app.carbpeek.feature.product.data

import app.carbpeek.core.database.product.ProductDao
import app.carbpeek.core.database.product.ProductEntity
import kotlinx.coroutines.flow.Flow

internal interface ProductPersistence {
    suspend fun find(barcode: String): ProductEntity?
    fun observe(barcode: String): Flow<ProductEntity?>
    fun observe(barcodes: List<String>): Flow<List<ProductEntity>>
    suspend fun save(product: ProductEntity)
}

internal class ProductPersistenceImpl(
    private val dao: ProductDao,
) : ProductPersistence {

    override suspend fun find(barcode: String): ProductEntity? = dao.findByBarcode(barcode)

    override fun observe(barcode: String): Flow<ProductEntity?> = dao.observeByBarcode(barcode)

    override fun observe(barcodes: List<String>): Flow<List<ProductEntity>> = dao.observeByBarcodes(barcodes)

    override suspend fun save(product: ProductEntity) = dao.upsert(product)
}
