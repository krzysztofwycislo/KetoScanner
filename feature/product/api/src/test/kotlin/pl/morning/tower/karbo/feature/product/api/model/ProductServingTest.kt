package pl.morning.tower.karbo.feature.product.api.model

import pl.morning.tower.karbo.core.model.ProductCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProductServingTest {

    @Test
    fun `when the serving is a positive number up to 500 then it is usable`() {
        assertEquals(30.0, product(servingAmount = "30").usableServingGrams)
        assertEquals(30.0, product(servingAmount = " 30 ").usableServingGrams)
        assertEquals(500.0, product(servingAmount = "500").usableServingGrams)
    }

    @Test
    fun `when the serving is over 500 or not positive then it is not usable`() {
        assertNull(product(servingAmount = "501").usableServingGrams)
        assertNull(product(servingAmount = "1000").usableServingGrams)
        assertNull(product(servingAmount = "0").usableServingGrams)
        assertNull(product(servingAmount = "-5").usableServingGrams)
    }

    @Test
    fun `when the serving is missing or not a number then it is not usable`() {
        assertNull(product(servingAmount = null).usableServingGrams)
        assertNull(product(servingAmount = " ").usableServingGrams)
        assertNull(product(servingAmount = "1 cup").usableServingGrams)
    }

    private fun product(servingAmount: String?) = Product(
        name = "Milk",
        barcode = "5900820000011",
        brand = "Dairy",
        nutriments = ProductNutriments(
            energyPerServing = null,
            energyPer100g = null,
            fatsPerServing = null,
            fatsPer100g = null,
            proteinsPerServing = null,
            proteinsPer100g = null,
            carbohydratesPerServing = null,
            carbohydratesPer100g = null,
            saltPerServing = null,
            saltPer100g = null,
        ),
        imageUrl = null,
        servingAmount = servingAmount,
        category = ProductCategory.OTHER,
    )
}
