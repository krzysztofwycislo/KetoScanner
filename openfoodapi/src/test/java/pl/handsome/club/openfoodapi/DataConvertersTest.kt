package pl.handsome.club.openfoodapi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.handsome.club.domain.product.ProductSearchState

class DataConvertersTest {

    @Test
    fun `convert success GetProductResponse with product into search product entity test`() {
        val response = getProductResponseWithProduct()
        val apiProduct = response.apiProduct!!

        val searchProduct = parseGetProductResponse(response)

        assertTrue(searchProduct is ProductSearchState.Success)

        val product = (searchProduct as ProductSearchState.Success).product
        assertEquals(apiProduct.productName, product.name)
        assertEquals(response.barcode, product.barcode)
    }

    @Test
    fun `convert GetProductResponse without product into search product entity test`() {
        val response = getProductResponseWithoutProduct()

        val searchProduct = parseGetProductResponse(response)

        assertTrue(searchProduct is ProductSearchState.NotFound)
    }

}