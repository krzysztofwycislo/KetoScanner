package pl.handsome.club.openfoodapi.data

import com.squareup.moshi.Json

data class ApiProduct(
    @Json(name = "nutriments") val nutriments: ApiNutriments,
    @Json(name = "emb_codes") val embCodes: String,
    @Json(name = "additives_tags") val additivesTags: List<String>,
    @Json(name = "ingredients_tags") val ingredientsTags: List<String>,
    @Json(name = "product_name") val productName: String,
    @Json(name = "image_url") val frontImage: String?,
    @Json(name = "brands") val brand: String,
    @Json(name = "serving_quantity") val servingQuantity: String
)
