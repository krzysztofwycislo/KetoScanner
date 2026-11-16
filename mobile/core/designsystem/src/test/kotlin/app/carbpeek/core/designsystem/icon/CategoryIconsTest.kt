package app.carbpeek.core.designsystem.icon

import app.carbpeek.core.model.ProductCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class CategoryIconsTest {

    @Test
    fun `other maps to bag`() {
        assertEquals(AppIcons.Bag, ProductCategory.OTHER.icon)
    }

    @Test
    fun `water and oils map to bottle`() {
        assertEquals(AppIcons.Bottle, ProductCategory.WATER.icon)
        assertEquals(AppIcons.Bottle, ProductCategory.OILS_AND_SAUCES.icon)
    }

    @Test
    fun `cheese maps to cheese`() {
        assertEquals(AppIcons.Cheese, ProductCategory.CHEESE.icon)
    }
}
