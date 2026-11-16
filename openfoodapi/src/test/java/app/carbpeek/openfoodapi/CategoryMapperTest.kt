package app.carbpeek.openfoodapi

import app.carbpeek.core.model.ProductCategory
import kotlin.test.Test
import kotlin.test.assertEquals

class CategoryMapperTest {

    @Test
    fun `each category is matched from realistic tags`() {
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
            listOf("en:cereals-and-their-products", "en:pastas", "en:spaghetti") to ProductCategory.GRAINS
        )

        cases.forEach { (tags, expected) ->
            assertEquals(expected, productCategoryOf(tags), "tags: $tags")
        }
    }

    @Test
    fun `cheese wins over its fermented milk parent`() {
        val tags = listOf("en:dairies", "en:fermented-milk-products", "en:cheeses")

        assertEquals(ProductCategory.CHEESE, productCategoryOf(tags))
    }

    @Test
    fun `chocolate biscuits are biscuits`() {
        val tags = listOf("en:snacks", "en:chocolates", "en:biscuits", "en:chocolate-biscuits")

        assertEquals(ProductCategory.BISCUITS, productCategoryOf(tags))
    }

    @Test
    fun `hazelnut spread without chocolate tag is a spread`() {
        val tags = listOf("en:breakfasts", "en:spreads", "en:sweet-spreads", "en:hazelnut-spreads")

        assertEquals(ProductCategory.SPREADS, productCategoryOf(tags))
    }

    @Test
    fun `tea wins over beverages`() {
        val tags = listOf("en:beverages", "en:hot-beverages", "en:teas", "en:green-teas")

        assertEquals(ProductCategory.TEA_AND_GREENS, productCategoryOf(tags))
    }

    @Test
    fun `mineral water wins over beverages`() {
        val tags = listOf("en:beverages", "en:waters", "en:mineral-waters")

        assertEquals(ProductCategory.WATER, productCategoryOf(tags))
    }

    @Test
    fun `null tags give other`() {
        assertEquals(ProductCategory.OTHER, productCategoryOf(null))
    }

    @Test
    fun `empty tags give other`() {
        assertEquals(ProductCategory.OTHER, productCategoryOf(emptyList()))
    }

    @Test
    fun `tags in other languages give other`() {
        assertEquals(ProductCategory.OTHER, productCategoryOf(listOf("pl:sery", "fr:fromages", "de:cheeses")))
    }

    @Test
    fun `unknown tags give other`() {
        assertEquals(ProductCategory.OTHER, productCategoryOf(listOf("en:soups", "en:frozen-foods")))
    }
}
