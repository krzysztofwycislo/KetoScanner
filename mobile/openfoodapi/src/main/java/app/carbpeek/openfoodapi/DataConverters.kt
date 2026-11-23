package app.carbpeek.openfoodapi

import app.carbpeek.feature.product.api.model.Ingredient
import app.carbpeek.feature.product.api.model.NovaGroup
import app.carbpeek.feature.product.api.model.NutriScoreGrade
import app.carbpeek.feature.product.api.model.Product
import app.carbpeek.feature.product.api.model.ProductNutriments
import app.carbpeek.openfoodapi.data.ApiIngredient
import app.carbpeek.openfoodapi.data.ApiNutriments
import app.carbpeek.openfoodapi.data.GetProductResponse

private const val ENGLISH = "en"
internal const val ENGLISH_TAG_PREFIX = "en:"
internal const val IN_TAXONOMY = 1
private const val E_NUMBER_PREFIX = "E"
private val E_NUMBER = Regex("e\\d+[a-z]*")

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
            ingredients = ingredients.toIngredients(language = language, labelText = ingredientsText),
            nutriScore = nutriScoreGradeOf(nutriScoreGrade),
            novaGroup = novaGroupOf(novaGroup),
        )
    }
}

internal fun List<ApiIngredient>?.toIngredients(language: String?, labelText: String?): List<Ingredient>? =
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
        }
        ?.takeIf { it.isNotEmpty() }

private fun ApiIngredient.toIngredient(language: String?, subIngredients: List<Ingredient>): Ingredient? {
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
        else -> tag.replace('-', ' ')
    }
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
