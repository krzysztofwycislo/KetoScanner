package pl.handsome.club.openfoodapi

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.handsome.club.feature.product.api.model.Product

internal class ProductMemoryCache(private val maxEntries: Int = 20) {

    private val mutex = Mutex()
    private val products = LinkedHashMap<String, Product>()

    suspend fun get(barcode: String): Product? = mutex.withLock { products[barcode] }

    suspend fun put(barcode: String, product: Product) {
        mutex.withLock {
            products.remove(barcode)
            products[barcode] = product
            while (products.size > maxEntries) {
                products.remove(products.keys.first())
            }
        }
    }
}
