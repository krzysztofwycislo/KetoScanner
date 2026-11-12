package pl.morning.tower.karbo.feature.analysis.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DietCriteriaTest {

    private val wholePlant = profile(fat = 0.5, protein = 1.0, carbs = 10.0, sugars = 3.0, fibre = 1.5)

    @Test
    fun `fibre and sugars at their carb shares make a whole plant food`() {
        assertTrue(wholePlant.isWholePlantFood())
    }

    @Test
    fun `too little fibre or sugars is not a whole plant food`() {
        assertFalse(wholePlant.copy(fibre = 1.49).isWholePlantFood())
        assertFalse(wholePlant.copy(sugars = 2.99).isWholePlantFood())
    }

    @Test
    fun `unknown fibre or sugars is not a whole plant food`() {
        assertFalse(wholePlant.copy(fibre = null).isWholePlantFood())
        assertFalse(wholePlant.copy(sugars = null).isWholePlantFood())
    }

    @Test
    fun `a liquid is not a whole plant food`() {
        assertFalse(wholePlant.copy(liquid = true).isWholePlantFood())
    }

    @Test
    fun `a product without carbs is not a whole plant food`() {
        assertFalse(
            profile(fat = 0.5, protein = 1.0, carbs = 0.0, sugars = 0.0, fibre = 1.5).isWholePlantFood(),
        )
    }
}
