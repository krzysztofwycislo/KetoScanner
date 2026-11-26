package app.carbpeek.server.product

internal val noNutrients = Nutrients(
    energyKcal = null,
    fat = null,
    saturatedFat = null,
    carbohydrates = null,
    sugars = null,
    fibre = null,
    proteins = null,
    salt = null,
)

internal fun minimalProduct(
    barcode: String,
    name: String? = null,
): Product =
    Product(
        barcode = barcode,
        name = name,
        brand = null,
        imageUrl = null,
        servingQuantity = null,
        category = ProductCategory.OTHER,
        labelLanguage = null,
        ingredientsText = null,
        ingredients = null,
        nutriScore = null,
        novaGroup = null,
        nutrientsPer100g = noNutrients,
        nutrientsPerServing = noNutrients,
    )
