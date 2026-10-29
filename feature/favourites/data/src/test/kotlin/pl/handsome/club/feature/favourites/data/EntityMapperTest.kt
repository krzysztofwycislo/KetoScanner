package pl.handsome.club.feature.favourites.data

import pl.handsome.club.core.database.favourite.FavouriteProductEntity
import pl.handsome.club.feature.favourites.api.model.FavouriteProduct
import pl.handsome.club.feature.product.api.model.Product
import pl.handsome.club.feature.product.api.model.ProductNutriments
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class EntityMapperTest {

    @Test
    fun `when entity is mapped then all fields are kept`() {
        val entity = FavouriteProductEntity(BARCODE, NAME, BRAND, IMAGE_URL).apply { updateTime = UPDATE_TIME }

        assertEquals(FavouriteProduct(BARCODE, NAME, BRAND, UPDATE_TIME, IMAGE_URL), entity.toDomain())
    }

    @Test
    fun `when entity list is mapped then order is kept`() {
        val entities = listOf("1", "2", "3").map { FavouriteProductEntity(it, NAME, BRAND, null) }

        assertEquals(listOf("1", "2", "3"), entities.toDomain().map { it.productBarcode })
    }

    @Test
    fun `when product is mapped to entity then barcode name brand and image are kept`() {
        val product = Product(
            name = NAME,
            barcode = BARCODE,
            brand = BRAND,
            nutriments = ProductNutriments(null, null, null, null, null, null, null, null, null, null),
            imageUrl = IMAGE_URL,
            servingAmount = null,
        )

        val entity = product.toFavouriteEntity()

        assertEquals(BARCODE, entity.productBarcode)
        assertEquals(NAME, entity.productName)
        assertEquals(BRAND, entity.productBrand)
        assertEquals(IMAGE_URL, entity.imageUrl)
    }

    private companion object {
        const val BARCODE = "5900000000001"
        const val NAME = "Peanut butter"
        const val BRAND = "Nuts & Co"
        const val IMAGE_URL = "https://example.com/image.jpg"
        val UPDATE_TIME = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}
