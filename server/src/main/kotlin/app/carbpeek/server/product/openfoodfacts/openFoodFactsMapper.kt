package app.carbpeek.server.product.openfoodfacts

import app.carbpeek.server.product.Ingredient
import app.carbpeek.server.product.NutriScore
import app.carbpeek.server.product.Nutrients
import app.carbpeek.server.product.Product

private const val ENGLISH = "en"
internal const val ENGLISH_TAG_PREFIX = "en:"
internal const val IN_TAXONOMY = 1
private const val E_NUMBER_PREFIX = "E"
private val E_NUMBER = Regex("e\\d+[a-z]*")
private val NOVA_GROUPS = 1..4

internal fun ApiProduct.toProduct(barcode: String): Product {
    val apiNutriments = nutriments ?: ApiNutriments()
    return Product(
        barcode = barcode,
        name = productName,
        brand = brand,
        imageUrl = frontImage,
        servingQuantity = servingQuantity,
        category = productCategoryOf(categoriesTags),
        labelLanguage = language,
        ingredientsText = ingredientsText,
        ingredients = ingredients.toIngredients(language = language, labelText = ingredientsText),
        nutriScore = nutriScoreOf(nutriScoreGrade),
        novaGroup = novaGroupOf(novaGroup),
        nutrientsPer100g = apiNutriments.toNutrientsPer100g(),
        nutrientsPerServing = apiNutriments.toNutrientsPerServing(),
    )
}

internal fun List<ApiIngredient>?.toIngredients(
    language: String?,
    labelText: String?,
): List<Ingredient>? =
    this
        ?.mapNotNull { ingredient ->
            val trustedNested = ingredient.ingredients
                ?.takeIf { isNestingTrusted(ingredient = ingredient, labelText = labelText) }
                .orEmpty()
            ingredient.toIngredient(
                language = language,
                subIngredients = trustedNested.mapNotNull {
                    it.toIngredient(language = language, subIngredients = emptyList())
                },
            )
        }?.takeIf { it.isNotEmpty() }

private fun ApiIngredient.toIngredient(
    language: String?,
    subIngredients: List<Ingredient>,
): Ingredient? {
    val labelName = text?.trim()
    if (labelName.isNullOrBlank()) {
        return null
    }
    val englishName = id
        ?.let(::englishIngredientNameOf)
        ?.takeIf { language != ENGLISH && isInTaxonomy == IN_TAXONOMY }
    return Ingredient(
        id = id ?: "",
        name = englishName ?: labelName,
        labelName = labelName,
        declaredPercent = percent?.trim()?.toDoubleOrNull(),
        subIngredients = subIngredients,
    )
}

internal fun englishIngredientNameOf(id: String): String? {
    if (!id.startsWith(ENGLISH_TAG_PREFIX)) {
        return null
    }
    val tag = id.removePrefix(ENGLISH_TAG_PREFIX).trim()
    return when {
        tag.isBlank() -> null
        tag.matches(E_NUMBER) -> E_NUMBER_PREFIX + tag.drop(1)
        else -> tag.replace(oldChar = '-', newChar = ' ')
    }
}

internal fun nutriScoreOf(value: String?): NutriScore? =
    NutriScore.entries.firstOrNull { it.name.equals(other = value?.trim(), ignoreCase = true) }

internal fun novaGroupOf(value: String?): Int? = value?.trim()?.toIntOrNull()?.takeIf { it in NOVA_GROUPS }

internal fun ApiNutriments.toNutrientsPer100g(): Nutrients =
    Nutrients(
        energyKcal = energyPer100g,
        fat = fatsPer100g,
        saturatedFat = saturatedFatPer100g,
        carbohydrates = carbohydratesPer100g,
        sugars = sugarsPer100g,
        fibre = fibrePer100g,
        proteins = proteinsPer100g,
        salt = saltPer100g,
    )

internal fun ApiNutriments.toNutrientsPerServing(): Nutrients =
    Nutrients(
        energyKcal = energyPerServing,
        fat = fatsPerServing,
        saturatedFat = saturatedFatPerServing,
        carbohydrates = carbohydratesPerServing,
        sugars = sugarsPerServing,
        fibre = fibrePerServing,
        proteins = proteinsPerServing,
        salt = saltPerServing,
    )
