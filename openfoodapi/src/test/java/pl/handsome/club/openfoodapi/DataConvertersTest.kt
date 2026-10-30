package pl.handsome.club.openfoodapi

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pl.handsome.club.feature.product.api.model.ProductNutriments
import pl.handsome.club.openfoodapi.data.ApiProduct
import pl.handsome.club.openfoodapi.data.GetProductResponse


class DataConvertersTest {

    @Test
    fun `convert success GetProductResponse with product into product test`() {
        val response = getProductResponseWithProduct()
        val apiProduct = response.apiProduct!!

        val product = parseGetProductResponse(response)!!

        assertEquals(apiProduct.productName, product.name)
        assertEquals(response.barcode, product.barcode)
    }

    @Test
    fun `convert GetProductResponse without product into null test`() {
        val response = getProductResponseWithoutProduct()

        assertNull(parseGetProductResponse(response))
    }

    @Test
    fun `parse nutriments maps per 100g and per serving values to matching fields`() {
        val nutriments = parseProductNutrients(testApiNutriments())

        assertThat(nutriments.carbohydratesPer100g).isEqualTo(44.0)
        assertThat(nutriments.carbohydratesPerServing).isEqualTo(22.0)
        assertThat(nutriments.energyPer100g).isEqualTo(512.0)
        assertThat(nutriments.energyPerServing).isEqualTo(206.0)
        assertThat(nutriments.fatsPer100g).isEqualTo(30.0)
        assertThat(nutriments.fatsPerServing).isEqualTo(15.0)
        assertThat(nutriments.proteinsPer100g).isEqualTo(20.0)
        assertThat(nutriments.proteinsPerServing).isEqualTo(10.0)
        assertThat(nutriments.saltPer100g).isEqualTo(3.0)
        assertThat(nutriments.saltPerServing).isEqualTo(1.5)
        assertThat(nutriments.sugarsPer100g).isEqualTo(10.0)
        assertThat(nutriments.sugarsPerServing).isEqualTo(5.0)
        assertThat(nutriments.fibrePer100g).isEqualTo(6.0)
        assertThat(nutriments.fibrePerServing).isEqualTo(3.0)
    }

    @Test
    fun `convert product without name and nutriments into product with empty name and null nutriments`() {
        val response = GetProductResponse(
            1,
            "barcode",
            ApiProduct(
                nutriments = null,
                productName = null,
                frontImage = null,
                brand = null,
                servingQuantity = null
            )
        )

        val product = parseGetProductResponse(response)

        assertThat(product).isNotNull()
        assertThat(product!!.name).isEmpty()
        assertThat(product.nutriments)
            .isEqualTo(ProductNutriments(null, null, null, null, null, null, null, null, null, null))
    }

}
