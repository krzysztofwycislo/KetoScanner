package app.carbpeek.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ProductCategoryTest {

    @Test
    fun `when name matches an entry then fromName returns that entry`() {
        assertEquals(ProductCategory.CHEESE, ProductCategory.fromName("CHEESE"))
    }

    @Test
    fun `when name is null then fromName returns other`() {
        assertEquals(ProductCategory.OTHER, ProductCategory.fromName(null))
    }

    @Test
    fun `when name is unknown then fromName returns other`() {
        assertEquals(ProductCategory.OTHER, ProductCategory.fromName("SOUPS"))
    }
}
