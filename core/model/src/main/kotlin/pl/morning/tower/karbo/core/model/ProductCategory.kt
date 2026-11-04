package pl.handsome.club.core.model

enum class ProductCategory {
    SPREADS,
    OILS_AND_SAUCES,
    WATER,
    DRINKS,
    MILK_AND_JUICE,
    YOGURT_AND_CREAM,
    CHOCOLATE_AND_BARS,
    CHEESE,
    EGGS,
    FISH,
    NUTS_AND_SEEDS,
    GRAINS,
    BISCUITS,
    TEA_AND_GREENS,
    MEAT,
    OTHER,
    ;

    companion object {
        fun fromName(name: String?): ProductCategory = entries.firstOrNull { it.name == name } ?: OTHER
    }
}
