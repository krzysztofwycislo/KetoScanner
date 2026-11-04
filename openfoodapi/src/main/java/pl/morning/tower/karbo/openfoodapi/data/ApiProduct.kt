package pl.morning.tower.karbo.openfoodapi.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiProduct(
    @SerialName("nutriments") val nutriments: ApiNutriments? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("image_url") val frontImage: String? = null,
    @SerialName("brands") val brand: String? = null,
    @SerialName("serving_quantity") val servingQuantity: String? = null,
    @SerialName("categories_tags") val categoriesTags: List<String>? = null
)
