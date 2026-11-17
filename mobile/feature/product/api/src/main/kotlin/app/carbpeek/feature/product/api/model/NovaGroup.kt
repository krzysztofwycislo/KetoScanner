package app.carbpeek.feature.product.api.model

enum class NovaGroup(val number: Int) {
    UNPROCESSED(1),
    CULINARY_INGREDIENT(2),
    PROCESSED(3),
    ULTRA_PROCESSED(4),
    ;

    companion object {
        fun fromNumber(number: Int?): NovaGroup? = entries.firstOrNull { it.number == number }
    }
}
