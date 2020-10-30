package pl.handsome.club.openfoodapi

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import pl.handsome.club.openfoodapi.api.OpenFoodFactsApiProvider


class OpenFoodApiTest {

    private val api = OpenFoodFactsApiProvider.getApi

    private val existingProductBarcode = "04963406"
    private val notExistingProductBarcode = "asd"


    @Test
    fun existing_product_api_search_test() {
        runBlocking {
            val getProductResponse = api.getProduct(existingProductBarcode)

            assertNotNull(getProductResponse)
            assertNotNull(getProductResponse.status == 1)
            assertNotNull(getProductResponse.apiProduct)
        }
    }

    @Test
    fun not_existing_product_api_search_test() {
        runBlocking {
            val getProductResponse = api.getProduct(notExistingProductBarcode)

            assertNotNull(getProductResponse)
            assertNotNull(getProductResponse.status == 0)
            assertNull(getProductResponse.apiProduct)
        }
    }

}