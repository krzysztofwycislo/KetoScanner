package app.carbpeek.openfoodapi

import app.carbpeek.feature.product.api.model.Ingredient
import app.carbpeek.feature.product.api.model.NovaGroup
import app.carbpeek.feature.product.api.model.NutriScoreGrade
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.openfoodapi.data.ApiIngredient
import app.carbpeek.openfoodapi.data.ApiNutriments
import app.carbpeek.openfoodapi.data.GetProductResponse


internal fun parseGetProductResponse(response: GetProductResponse): Product? {
    val apiProduct = response.apiProduct ?: return null

    return with(apiProduct) {
        val productNutriments = parseProductNutrients(nutriments ?: ApiNutriments())

        Product(
            name = productName ?: "",
            barcode = response.barcode,
            brand = brand ?: "",
            nutriments = productNutriments,
            imageUrl = frontImage,
            servingAmount = servingQuantity,
            category = productCategoryOf(categoriesTags),
            ingredients = ingredients.toIngredients(),
            nutriScore = nutriScoreGradeOf(nutriScoreGrade),
            novaGroup = novaGroupOf(novaGroup),
        )
    }
}

internal fun List<ApiIngredient>?.toIngredients(): List<Ingredient>? =
    this
        ?.mapNotNull { it.toIngredient() }
        ?.takeIf { it.isNotEmpty() }

private fun ApiIngredient.toIngredient(): Ingredient? {
    val name = text?.trim()
    if (name.isNullOrBlank()) {
        return null
    }
    return Ingredient(
        id = id ?: "",
        name = name,
        declaredPercent = percent?.trim()?.toDoubleOrNull(),
        subIngredients = ingredients.toIngredients().orEmpty(),
    )
}

internal fun nutriScoreGradeOf(value: String?): NutriScoreGrade? =
    NutriScoreGrade.entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) }

internal fun novaGroupOf(value: String?): NovaGroup? = NovaGroup.fromNumber(value?.trim()?.toIntOrNull())

fun parseProductNutrients(apiNutriments: ApiNutriments): ProductNutriments =
    with(apiNutriments) {
        ProductNutriments(
            energyPerServing = energyPerServing,
            energyPer100g = energyPer100g,
            fatsPerServing = fatsPerServing,
            fatsPer100g = fatsPer100g,
            proteinsPerServing = proteinsPerServing,
            proteinsPer100g = proteinsPer100g,
            carbohydratesPerServing = carbohydratesPerServing,
            carbohydratesPer100g = carbohydratesPer100g,
            saltPerServing = saltPerServing,
            saltPer100g = saltPer100g,
            sugarsPerServing = sugarsPerServing,
            sugarsPer100g = sugarsPer100g,
            fibrePerServing = fibrePerServing,
            fibrePer100g = fibrePer100g,
        )
    }
