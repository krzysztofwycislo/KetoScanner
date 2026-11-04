package pl.morning.tower.karbo.openfoodapi

import pl.morning.tower.karbo.core.model.ProductCategory

private val categoryTags: List<Pair<ProductCategory, Set<String>>> = listOf(
    ProductCategory.CHEESE to setOf("en:cheeses"),
    ProductCategory.EGGS to setOf("en:eggs"),
    ProductCategory.FISH to setOf("en:fishes", "en:seafood"),
    ProductCategory.MEAT to setOf(
        "en:meats",
        "en:prepared-meats",
        "en:hams",
        "en:sausages",
        "en:bacon",
        "en:poultries"
    ),
    ProductCategory.BISCUITS to setOf("en:biscuits", "en:cookies", "en:biscuits-and-cakes", "en:crackers"),
    ProductCategory.CHOCOLATE_AND_BARS to setOf("en:chocolates", "en:bars", "en:protein-bars", "en:cereal-bars"),
    ProductCategory.SPREADS to setOf(
        "en:spreads",
        "en:sweet-spreads",
        "en:jams",
        "en:honeys",
        "en:nut-butters",
        "en:peanut-butters"
    ),
    ProductCategory.NUTS_AND_SEEDS to setOf("en:nuts", "en:seeds"),
    ProductCategory.YOGURT_AND_CREAM to setOf("en:yogurts", "en:creams", "en:kefirs", "en:dairy-desserts"),
    ProductCategory.MILK_AND_JUICE to setOf("en:milks", "en:plant-milks", "en:juices", "en:fruit-juices"),
    ProductCategory.OILS_AND_SAUCES to setOf(
        "en:vegetable-oils",
        "en:olive-oils",
        "en:sauces",
        "en:condiments",
        "en:vinegars",
        "en:mayonnaises",
        "en:ketchup"
    ),
    ProductCategory.TEA_AND_GREENS to setOf("en:teas", "en:herbal-teas", "en:herbs", "en:salads", "en:vegetables"),
    ProductCategory.WATER to setOf("en:waters", "en:mineral-waters", "en:spring-waters"),
    ProductCategory.DRINKS to setOf(
        "en:sodas",
        "en:carbonated-drinks",
        "en:soft-drinks",
        "en:energy-drinks",
        "en:beverages"
    ),
    ProductCategory.GRAINS to setOf(
        "en:pastas",
        "en:breads",
        "en:rices",
        "en:breakfast-cereals",
        "en:flours",
        "en:cereals-and-their-products"
    )
)

internal fun productCategoryOf(tags: List<String>?): ProductCategory {
    if (tags.isNullOrEmpty()) return ProductCategory.OTHER
    return categoryTags.firstOrNull { (_, categoryTags) -> tags.any { it in categoryTags } }?.first
        ?: ProductCategory.OTHER
}
