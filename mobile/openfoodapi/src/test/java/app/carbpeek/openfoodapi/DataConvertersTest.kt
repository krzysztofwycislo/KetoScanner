package app.carbpeek.openfoodapi

import app.carbpeek.core.model.ProductCategory
import app.carbpeek.feature.product.api.model.NovaGroup
import app.carbpeek.feature.product.api.model.NutriScoreGrade
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.openfoodapi.data.ApiIngredient
import app.carbpeek.openfoodapi.data.ApiProduct
import app.carbpeek.openfoodapi.data.GetProductResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class DataConvertersTest {

    @Test
    fun `convert success GetProductResponse with product into product test`() {
        val response = getProductResponseWithProduct()
        val apiProduct = response.apiProduct!!

        val product = parseGetProductResponse(response)!!

        assertEquals(apiProduct.productName, product.name)
        assertEquals(response.barcode, product.barcode)
        assertEquals(ProductCategory.CHEESE, product.category)
    }

    @Test
    fun `convert GetProductResponse without product into null test`() {
        val response = getProductResponseWithoutProduct()

        assertNull(parseGetProductResponse(response))
    }

    @Test
    fun `parse nutriments maps per 100g and per serving values to matching fields`() {
        val nutriments = parseProductNutrients(testApiNutriments())

        assertEquals(44.0, nutriments.carbohydratesPer100g)
        assertEquals(22.0, nutriments.carbohydratesPerServing)
        assertEquals(512.0, nutriments.energyPer100g)
        assertEquals(206.0, nutriments.energyPerServing)
        assertEquals(30.0, nutriments.fatsPer100g)
        assertEquals(15.0, nutriments.fatsPerServing)
        assertEquals(20.0, nutriments.proteinsPer100g)
        assertEquals(10.0, nutriments.proteinsPerServing)
        assertEquals(3.0, nutriments.saltPer100g)
        assertEquals(1.5, nutriments.saltPerServing)
        assertEquals(10.0, nutriments.sugarsPer100g)
        assertEquals(5.0, nutriments.sugarsPerServing)
        assertEquals(6.0, nutriments.fibrePer100g)
        assertEquals(3.0, nutriments.fibrePerServing)
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

        assertNotNull(product)
        assertTrue(product.name.isEmpty())
        assertEquals(ProductNutriments(null, null, null, null, null, null, null, null, null, null), product.nutriments)
        assertEquals(ProductCategory.OTHER, product.category)
    }

    @Test
    fun `nutri score grades a to e are read in any case and anything else is null`() {
        assertEquals(NutriScoreGrade.A, nutriScoreGradeOf("A"))
        assertEquals(NutriScoreGrade.C, nutriScoreGradeOf("c"))
        assertNull(nutriScoreGradeOf("unknown"))
        assertNull(nutriScoreGradeOf("not-applicable"))
        assertNull(nutriScoreGradeOf(null))
    }

    @Test
    fun `nova groups 1 to 4 are read and anything else is null`() {
        assertEquals(NovaGroup.ULTRA_PROCESSED, novaGroupOf("4"))
        assertNull(novaGroupOf("7"))
        assertNull(novaGroupOf("x"))
        assertNull(novaGroupOf(null))
    }

    @Test
    fun `an empty or textless ingredient list becomes null`() {
        assertNull(emptyList<ApiIngredient>().toIngredients())
        assertNull(listOf(ApiIngredient(id = "en:sugar", text = " ")).toIngredients())
    }

    @Test
    fun `api product without ingredients and scores converts to nulls`() {
        val product = assertNotNull(parseGetProductResponse(getProductResponseWithProduct()))

        assertNull(product.ingredients)
        assertNull(product.nutriScore)
        assertNull(product.novaGroup)
    }

}
