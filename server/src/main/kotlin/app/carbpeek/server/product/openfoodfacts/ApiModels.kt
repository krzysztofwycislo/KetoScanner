package app.carbpeek.server.product.openfoodfacts

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GetProductResponse(
    @SerialName("status") val status: Int? = null,
    @SerialName("product") val product: ApiProduct? = null,
)

@Serializable
internal data class ApiProduct(
    @SerialName("nutriments") val nutriments: ApiNutriments? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("image_url") val frontImage: String? = null,
    @SerialName("brands") val brand: String? = null,
    @SerialName("serving_quantity") val servingQuantity: String? = null,
    @SerialName("categories_tags") val categoriesTags: List<String>? = null,
    @SerialName("ingredients") val ingredients: List<ApiIngredient>? = null,
    @SerialName("ingredients_text") val ingredientsText: String? = null,
    @SerialName("nutriscore_grade") val nutriScoreGrade: String? = null,
    @SerialName("nova_group") val novaGroup: String? = null,
    @SerialName("lang") val language: String? = null,
)

@Serializable
internal data class ApiIngredient(
    @SerialName("id") val id: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("percent") val percent: String? = null,
    @SerialName("is_in_taxonomy") val isInTaxonomy: Int? = null,
    @SerialName("ingredients") val ingredients: List<ApiIngredient>? = null,
)

@Serializable
internal data class ApiNutriments(
    @SerialName("energy-kcal_100g") val energyPer100g: Double? = null,
    @SerialName("energy-kcal_serving") val energyPerServing: Double? = null,
    @SerialName("fat_100g") val fatsPer100g: Double? = null,
    @SerialName("fat_serving") val fatsPerServing: Double? = null,
    @SerialName("saturated-fat_100g") val saturatedFatPer100g: Double? = null,
    @SerialName("saturated-fat_serving") val saturatedFatPerServing: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydratesPer100g: Double? = null,
    @SerialName("carbohydrates_serving") val carbohydratesPerServing: Double? = null,
    @SerialName("sugars_100g") val sugarsPer100g: Double? = null,
    @SerialName("sugars_serving") val sugarsPerServing: Double? = null,
    @SerialName("proteins_100g") val proteinsPer100g: Double? = null,
    @SerialName("proteins_serving") val proteinsPerServing: Double? = null,
    @SerialName("salt_100g") val saltPer100g: Double? = null,
    @SerialName("salt_serving") val saltPerServing: Double? = null,
    @SerialName("fiber_100g") val fibrePer100g: Double? = null,
    @SerialName("fiber_serving") val fibrePerServing: Double? = null,
)
