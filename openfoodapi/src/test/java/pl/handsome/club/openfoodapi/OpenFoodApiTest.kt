package pl.handsome.club.openfoodapi

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test


class OpenFoodApiTest {

    private val api = OpenFoodFactsApiProvider.getApi

    private val existingProductBarcode = "04963406"


    @Test
    fun when_we_want_to_get_existing_product_from_api_by_barcode_then_it_should_be_returned() {
        runBlocking {
            val product = api.getProduct(existingProductBarcode)
            assertNotNull(product)
        }
    }

}