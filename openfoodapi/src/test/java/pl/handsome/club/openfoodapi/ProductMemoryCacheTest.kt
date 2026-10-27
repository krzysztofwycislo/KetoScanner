package pl.handsome.club.openfoodapi

import kotlinx.coroutines.runBlocking
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductMemoryCacheTest {

    @Test
    fun `when barcode was never put then get returns null`() = runBlocking {
        assertNull(ProductMemoryCache().get("1"))
    }

    @Test
    fun `when product is put then get returns it`() = runBlocking {
        val cache = ProductMemoryCache()

        cache.put("1", product("1"))

        assertEquals(product("1"), cache.get("1"))
    }

    @Test
    fun `when more than max entries are put then the oldest entry is evicted`() = runBlocking {
        val cache = ProductMemoryCache(maxEntries = 2)

        cache.put("1", product("1"))
        cache.put("2", product("2"))
        cache.put("3", product("3"))

        assertNull(cache.get("1"))
        assertEquals(product("2"), cache.get("2"))
        assertEquals(product("3"), cache.get("3"))
    }

    @Test
    fun `when an existing key is put again then it is kept as the newest entry`() = runBlocking {
        val cache = ProductMemoryCache(maxEntries = 2)

        cache.put("1", product("1"))
        cache.put("2", product("2"))
        cache.put("1", product("1"))
        cache.put("3", product("3"))

        assertEquals(product("1"), cache.get("1"))
        assertNull(cache.get("2"))
        assertEquals(product("3"), cache.get("3"))
    }

    private companion object {
        fun product(barcode: String) = Product(
            name = "Name $barcode",
            barcode = barcode,
            brand = "Brand",
            nutriments = ProductNutriments(null, null, null, null, null, null, null, null, null, null),
            imageUrl = null,
            servingAmount = null,
        )
    }
}
