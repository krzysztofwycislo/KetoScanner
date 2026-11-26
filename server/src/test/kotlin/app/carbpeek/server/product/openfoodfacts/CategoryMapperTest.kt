package app.carbpeek.server.product.openfoodfacts

import app.carbpeek.server.product.ProductCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class CategoryMapperTest {

    @Test
    fun `when tags are realistic then each category is matched`() {
        val cases = mapOf(
            listOf("en:dairies", "en:cheeses", "en:hard-cheeses") to ProductCategory.CHEESE,
            listOf("en:farming-products", "en:eggs", "en:chicken-eggs") to ProductCategory.EGGS,
            listOf("en:seafood", "en:fishes", "en:smoked-fishes") to ProductCategory.FISH,
            listOf("en:meats", "en:prepared-meats", "en:hams") to ProductCategory.MEAT,
            listOf("en:snacks", "en:sweet-snacks", "en:biscuits") to ProductCategory.BISCUITS,
            listOf("en:snacks", "en:cocoa-and-its-products", "en:chocolates", "en:dark-chocolates") to
                ProductCategory.CHOCOLATE_AND_BARS,
            listOf("en:spreads", "en:sweet-spreads", "en:honeys") to ProductCategory.SPREADS,
            listOf("en:plant-based-foods", "en:nuts", "en:almonds") to ProductCategory.NUTS_AND_SEEDS,
            listOf("en:dairies", "en:fermented-milk-products", "en:yogurts") to ProductCategory.YOGURT_AND_CREAM,
            listOf("en:dairies", "en:milks", "en:whole-milks") to ProductCategory.MILK_AND_JUICE,
            listOf("en:fats", "en:vegetable-oils", "en:olive-oils") to ProductCategory.OILS_AND_SAUCES,
            listOf("en:plant-based-foods", "en:vegetables", "en:leaf-vegetables") to ProductCategory.TEA_AND_GREENS,
            listOf("en:beverages", "en:waters", "en:mineral-waters") to ProductCategory.WATER,
            listOf("en:beverages", "en:carbonated-drinks", "en:sodas") to ProductCategory.DRINKS,
            listOf("en:cereals-and-their-products", "en:pastas", "en:spaghetti") to ProductCategory.GRAINS,
        )

        cases.forEach { (tags, expected) ->
            assertEquals(expected = expected, actual = productCategoryOf(tags), message = "tags: $tags")
        }
    }

    @Test
    fun `when cheese has a fermented milk parent then cheese wins`() {
        val tags = listOf("en:dairies", "en:fermented-milk-products", "en:cheeses")

        assertEquals(expected = ProductCategory.CHEESE, actual = productCategoryOf(tags))
    }

    @Test
    fun `when biscuits have chocolate then they are biscuits`() {
        val tags = listOf("en:snacks", "en:chocolates", "en:biscuits", "en:chocolate-biscuits")

        assertEquals(expected = ProductCategory.BISCUITS, actual = productCategoryOf(tags))
    }

    @Test
    fun `when a hazelnut spread has no chocolate tag then it is a spread`() {
        val tags = listOf("en:breakfasts", "en:spreads", "en:sweet-spreads", "en:hazelnut-spreads")

        assertEquals(expected = ProductCategory.SPREADS, actual = productCategoryOf(tags))
    }

    @Test
    fun `when tea is a beverage then tea wins`() {
        val tags = listOf("en:beverages", "en:hot-beverages", "en:teas", "en:green-teas")

        assertEquals(expected = ProductCategory.TEA_AND_GREENS, actual = productCategoryOf(tags))
    }

    @Test
    fun `when mineral water is a beverage then water wins`() {
        val tags = listOf("en:beverages", "en:waters", "en:mineral-waters")

        assertEquals(expected = ProductCategory.WATER, actual = productCategoryOf(tags))
    }

    @Test
    fun `when tags are null then the category is other`() {
        assertEquals(expected = ProductCategory.OTHER, actual = productCategoryOf(null))
    }

    @Test
    fun `when tags are empty then the category is other`() {
        assertEquals(expected = ProductCategory.OTHER, actual = productCategoryOf(emptyList()))
    }

    @Test
    fun `when tags are in other languages then the category is other`() {
        val tags = listOf("pl:sery", "fr:fromages", "de:cheeses")

        assertEquals(expected = ProductCategory.OTHER, actual = productCategoryOf(tags))
    }

    @Test
    fun `when tags are unknown then the category is other`() {
        val tags = listOf("en:soups", "en:frozen-foods")

        assertEquals(expected = ProductCategory.OTHER, actual = productCategoryOf(tags))
    }
}
