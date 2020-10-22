package pl.handsome.club.ketoscanner.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.handsome.club.ketoscanner.data.testGetProductResponse

// TODO refactor
class DataConvertersTest {

    @Test
    fun `when we want to convert get product api response into product then data should be correct`() {
        val response = testGetProductResponse()

        val product = convertToProduct(response)

        with(response) {
            assertEquals(apiProduct.productName, product.name)
            assertEquals(barcode, product.barcode)
        }
    }

}