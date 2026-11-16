package app.carbpeek.feature.product.api.model

private const val MAX_SERVING_GRAMS = 500.0

val Product.usableServingGrams: Double?
    get() = servingAmount?.trim()?.toDoubleOrNull()?.takeIf { it > 0 && it <= MAX_SERVING_GRAMS }
